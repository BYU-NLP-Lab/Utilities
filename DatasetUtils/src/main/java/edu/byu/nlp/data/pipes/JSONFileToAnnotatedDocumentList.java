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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
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

	private static final Logger logger = LoggerFactory.getLogger(JSONFileToAnnotatedDocumentList.class);

	private Reader jsonReader;
	private String jsonReferencedDataDir;

	public JSONFileToAnnotatedDocumentList(String basedir) throws FileNotFoundException {
		this(basedir, Charset.defaultCharset());
	}

	public JSONFileToAnnotatedDocumentList(String jsonFile, Charset charset)
			throws FileNotFoundException {
		// assume the datadir referred to by json will be relative to the parent folder of the dataset (e.g., if 
		// jsonFile= /aml/data/plf1/cfgroups/cfgroups1000.json then the basedir should be /aml/data/plf1
		this(new BufferedReader(new InputStreamReader(new FileInputStream(jsonFile), charset)), new File(jsonFile).getParentFile().getParent());
	}

	public JSONFileToAnnotatedDocumentList(Reader jsonReader, String jsonReferencedDataDir) {
		Preconditions.checkNotNull(jsonReader);
		this.jsonReader = jsonReader;
		this.jsonReferencedDataDir=jsonReferencedDataDir;
	}

	// simple deserialization pojo
	private static class JSONAnnotation {
		private String annotator, label, data, source, annotation, datapath;
		private long starttime = -1, endtime = -1;
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

		List<JSONAnnotation> jsonData = gson.fromJson(this.jsonReader, collectiontype);

		// translate annotations into flatInstances 
		// and gather instance data 
		Map<String,InstancePojo> instanceData = Maps.newHashMap();
		List<FlatInstance<String, String>> transformedAnnotations = Lists.newArrayList();
		for (JSONAnnotation ann : jsonData) {
			
			// annotation
			if (ann.annotation != null) {
				String annotationData = null; // data will be passed on only via the labeledinstance to avoid redundant processing 
				transformedAnnotations.add(new FlatAnnotatedInstance<String,String>(
						AnnotationInterfaceJavaUtils.newAnnotatedInstance(
								ann.annotator, ann.annotation, ann.starttime * 1000 * 1000, ann.endtime * 1000 * 1000, 
								ann.source, annotationData)));
			}
			
			
			// ensure 1 instance per unique source
			if (!instanceData.containsKey(ann.source)){
				instanceData.put(ann.source, new InstancePojo());
			}
			InstancePojo inst = instanceData.get(ann.source);
			// gather instance info
			inst.source=ann.source;
			if (inst.data==null){
				inst.data = ann.data;
			}
			if (inst.datapath==null){
				inst.datapath=ann.datapath;
			}
			if (inst.label == null) {
				inst.label = ann.label;
				inst.labelobserved = ann.labelobserved;
			}
			
		}

		// create exactly 1 labeled instance for each unique source (even if the label is null)
		List<FlatInstance<String, String>> transformedInstances = Lists.newArrayList();
		for (InstancePojo pojo: instanceData.values()){
			
			// read data from disk (if necessary)
			String instData = pojo.data;
			if (instData == null) {
				List<String> lines;
				File jsonpath = new File(jsonReferencedDataDir, pojo.datapath);
				try {
					lines = Files.readLines(jsonpath, Charset.forName("utf-8"));
				} catch (IOException e) {
					throw new RuntimeException("unable to read file " + jsonpath.getAbsolutePath(), e);
				}
				instData = Strings.join(lines, "\n");
			}

			transformedInstances.add(new FlatLabeledInstance<String,String>(
					AnnotationInterfaceJavaUtils.newLabeledInstance(instData, pojo.label, pojo.source, !pojo.labelobserved)));
		}
		
		return Iterators.concat(transformedAnnotations.iterator(), transformedInstances.iterator());
		
	}


}
