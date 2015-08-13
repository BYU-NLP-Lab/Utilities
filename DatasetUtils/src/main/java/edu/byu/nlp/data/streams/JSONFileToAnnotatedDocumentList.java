/**
 * 
 */
package edu.byu.nlp.data.streams;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import edu.byu.nlp.data.streams.DataStreams.OneToMany;
import edu.byu.nlp.data.types.DataStreamInstance;
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
public class JSONFileToAnnotatedDocumentList implements OneToMany {

	private static final Logger logger = LoggerFactory.getLogger(JSONFileToAnnotatedDocumentList.class);

	private String jsonReferencedDataDir;
  private String fieldname;

	public JSONFileToAnnotatedDocumentList(String jsonReferencedDataDir, String fieldname) {
		this.jsonReferencedDataDir=jsonReferencedDataDir;
		this.fieldname=fieldname;
	}

	// simple deserialization pojo
	public static class MeasurementPojo {
	  public String type;
	  public String label;
	  public double value;
	  public Double confidence;
	  public String predicate;
	}
	private static class AnnotationPojo {
	  public String annotator, label, data, source, annotation, datapath;
	  public MeasurementPojo measurement;
	  public long starttime = -1, endtime = -1;
	  public boolean labelobserved;

		@Override
		public String toString() {
			return getClass().getName()+" src="+source;
		}
		
		@SuppressWarnings("unused")
		public AnnotationPojo() {
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
  public Iterable<Map<String, Object>> apply(Map<String, Object> input) {
	  // this should be the only thing the input has
	  String indexFilename = (String)input.get(fieldname);
	  
		logger.info("Processing " + indexFilename);
		Reader jsonReader = readerOf(indexFilename);

		// parse json
		Gson gson = new Gson();
		Type collectiontype = new TypeToken<List<AnnotationPojo>>(){}.getType();

		List<AnnotationPojo> jsonData = gson.fromJson(jsonReader, collectiontype);

		// translate annotations into flatInstances 
		// and gather instance data 
		Map<String,InstancePojo> instanceData = Maps.newHashMap();
		List<Map<String,Object>> transformedAnnotations = Lists.newArrayList();
		for (AnnotationPojo ann : jsonData) {
			
			// annotation
			if (ann.annotation != null) {
				// data will be passed on only via the labeledinstance to avoid redundant processing 
				transformedAnnotations.add(DataStreamInstance.fromAnnotationRaw(ann.source, ann.source, ann.annotator, ann.annotation, 
				    ann.starttime * 1000 * 1000, ann.endtime * 1000 * 1000, ann.measurement));
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
		List<Map<String,Object>> transformedInstances = Lists.newArrayList();
		for (InstancePojo pojo: instanceData.values()){
			
			// read data from disk (if necessary)
			String instData = pojo.data;
			if (instData == null && pojo.datapath!=null) {
				List<String> lines;
				File jsonpath = new File(jsonReferencedDataDir, pojo.datapath);
				try {
					lines = Files.readLines(jsonpath, Charset.forName("utf-8"));
				} catch (IOException e) {
					throw new RuntimeException("unable to read file " + jsonpath.getAbsolutePath(), e);
				}
				instData = Strings.join(lines, "\n");
			}

			transformedInstances.add(DataStreamInstance.fromLabelRaw(pojo.source, pojo.source, instData, pojo.label, pojo.labelobserved));
		}
		
		return Iterables.concat(transformedAnnotations, transformedInstances);
		
	}

  private static Reader readerOf(String jsonFile) {
    try {
      return new BufferedReader(new InputStreamReader(new FileInputStream(jsonFile),"utf-8"));
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Invalid json file",e);
    } catch (FileNotFoundException e) {
      throw new RuntimeException("Non-existent json file",e);
    }
  }


}
