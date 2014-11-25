/**
 * 
 */
package edu.byu.nlp.data.pipes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import org.apache.commons.math3.random.RandomGenerator;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import edu.byu.nlp.annotationinterface.java.AnnotationInterfaceJavaUtils;
import edu.byu.nlp.data.FlatAnnotatedInstance;
import edu.byu.nlp.data.FlatInstance;
import edu.byu.nlp.data.FlatLabeledInstance;
import edu.byu.nlp.data.pipes.Instances.OneToManyLabeledInstanceFunction;
import edu.byu.nlp.util.Strings;

/**
 * Parses a list of annotated documents from a JSON file with the following
 * structure:
 * 
 * [ # An annotated instance { batch: 123 source: "http://document/id", data:
 * "The text of the first document", label: "TrueLabel", annotator: "george",
 * annotation: "SomeLabel" annotationTime: { "startTimeSecs":1319123,
 * "endTimeSecs":1319198} }, etc... ]
 *
 * If 'batch' is set, this annotation was received as part of a batch of
 * annotations sharing this number. Annotations in the same batch are reported
 * consecutively.
 * 
 * startTimeSecs and endTimeSecs are utc timestamps (number of secs since 1 Jan
 * 1970))
 * 
 * @author pfelt
 * 
 */
public class JSONFileToAnnotatedDocumentList implements OneToManyLabeledInstanceFunction<String, String, String> {

	private static Logger logger = Logger.getLogger(JSONFileToAnnotatedDocumentList.class.getName());

	private Reader jsonReader;
	private RandomGenerator rnd;

	public JSONFileToAnnotatedDocumentList(String basedir, RandomGenerator rnd) throws FileNotFoundException {
		this(basedir, Charset.defaultCharset(), rnd);
	}

	public JSONFileToAnnotatedDocumentList(String jsonFile, Charset charset, RandomGenerator rnd)
			throws FileNotFoundException {
		this(new BufferedReader(new InputStreamReader(new FileInputStream(jsonFile), charset)), rnd);
	}

	public JSONFileToAnnotatedDocumentList(Reader jsonReader, RandomGenerator rnd) {
		Preconditions.checkNotNull(jsonReader);
		this.jsonReader = jsonReader;
		this.rnd = rnd;
	}

	// simple deserialization pojo
	private static class JSONAnnotation {
		private String annotator, label, data, source, annotation, datapath;
		private long startTime, endTime;
		private boolean labelobserved;

		@Override
		public String toString() {
			return getClass().getName()+" src="+source;
		}
		
		@SuppressWarnings("unused")
		public JSONAnnotation() {
		}
	}

	private class InstancePojo {
		private List<JSONAnnotation> annotations = Lists.newArrayList();
		private String label, data, source, datapath;
		private boolean labelobserved;
		@Override
		public String toString() {
			return getClass().getName()+" src="+source;
		}
	}

	@Override
	public Iterator<FlatInstance<String, String>> apply(FlatInstance<String, String> indexFilename) {
		logger.info("Processing " + indexFilename.getData());

		// parse json
		Gson gson = new Gson();
		Type collectiontype = new TypeToken<List<JSONAnnotation>>() {
		}.getType();

		List<JSONAnnotation> data = gson.fromJson(this.jsonReader, collectiontype);

		// aggregate items that share a data source into a single instance
		Map<String, InstancePojo> instanceMap = Maps.newHashMap();
		for (JSONAnnotation ann : data) {
			if (!instanceMap.containsKey(ann.source)) {
				instanceMap.put(ann.source, new InstancePojo());
			}
			InstancePojo inst = instanceMap.get(ann.source);
			inst.data = ann.data;
			inst.datapath = ann.datapath;
			inst.source = ann.source;
			// annotation
			if (ann.annotation != null) {
				inst.annotations.add(ann);
			}
			// instance (with optional gold label)
			if (ann.label != null) {
				if (inst.label != null && !inst.label.equals(ann.label)) {
					logger.warning("Multiple differing labels have been specified for the same data instance. ("
							+ inst.label + " and " + ann.label + ") Choosing " + ann.label + " arbitrarily");
				}
				inst.label = ann.label;
				inst.labelobserved = ann.labelobserved;
			}
		}

		// separate labeled (gold standard label) from unlabeled instances
		List<InstancePojo> observedLabelInstances = Lists.newArrayList();
		List<InstancePojo> labeledInstances = Lists.newArrayList();
		List<InstancePojo> unlabeledInstances = Lists.newArrayList();
		List<String> docSources = Lists.newArrayList(instanceMap.keySet());
		Collections.shuffle(docSources, new Random(rnd.nextLong())); // random
																		// instance
																		// order
																		// within
																		// categories
		for (String docSource : docSources) {
			InstancePojo pojo = instanceMap.get(docSource);
			if (pojo.labelobserved) {
				observedLabelInstances.add(instanceMap.get(docSource));
			} else if (pojo.label != null) {
				labeledInstances.add(instanceMap.get(docSource));
			} else {
				unlabeledInstances.add(instanceMap.get(docSource));
			}
		}

		List<FlatInstance<String, String>> transformedInstances = Lists.newArrayList();
		List<FlatInstance<String, String>> transformedAnnotations = Lists.newArrayList();
		
		// assemble iterators over flatInstances
		for (InstancePojo pojo: Iterables.concat(observedLabelInstances, labeledInstances, unlabeledInstances)){

			// read data from disk (if necessary)
			String instData = pojo.data;
			if (data == null) {
				List<String> lines;
				try {
					lines = Files.readLines(new File(pojo.datapath), Charset.forName("utf-8"));
				} catch (IOException e) {
					throw new RuntimeException("unable to read file " + pojo.datapath, e);
				}
				instData = Strings.join(lines, "\n");
			}
			
			// add annotated instances
			for (JSONAnnotation ann : pojo.annotations) {
				transformedAnnotations.add(new FlatAnnotatedInstance<String,String>(
						AnnotationInterfaceJavaUtils.newAnnotatedInstance(
								ann.annotator, ann.annotation, ann.startTime * 1000 * 1000, ann.endTime * 1000 * 1000, 
								ann.source, instData)));
			}

			// add a labeled instance
			transformedInstances.add(new FlatLabeledInstance<String,String>(
					AnnotationInterfaceJavaUtils.newLabeledInstance(instData, pojo.label, pojo.source, !pojo.labelobserved)));
			
		}
		
		
		return Iterators.concat(transformedInstances.iterator(), transformedAnnotations.iterator());
		
	}

//	private static FlatInstance<String, String> jsonPojo2Annotation(InstancePojo pojo, List<Annotation<String, String>> annotations) {
//
//		// read data from disk (if necessary)
//		String data = pojo.data;
//		if (data == null) {
//			List<String> lines;
//			try {
//				lines = Files.readLines(new File(pojo.datapath), Charset.forName("utf-8"));
//			} catch (IOException e) {
//				throw new RuntimeException("unable to read file " + pojo.datapath, e);
//			}
//			data = Strings.join(lines, "\n");
//		}
//		
//		// add annotations to the list that was passed in
//		for (JSONAnnotation ann : pojo.annotations) {
//			annotations.add(AnnotationInterfaceJavaUtils.newAnnotatedInstance(
//				ann.annotator, ann.annotation, ann.startTime, ann.endTime, pojo.source, data));
//		}
//
//		// return an instance
//		return new FlatLabeledInstance<String,String>(
//				AnnotationInterfaceJavaUtils.newLabeledInstance(data, pojo.label, pojo.source));
////		BasicInstance.of(pojo.label, pojo.labelobserved, null, pojo.source, data, annotations);
//	}

}
