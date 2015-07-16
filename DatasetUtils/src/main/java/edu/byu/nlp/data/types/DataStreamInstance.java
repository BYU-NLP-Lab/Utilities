package edu.byu.nlp.data.types;

import java.util.Map;

import edu.byu.nlp.data.measurements.ClassificationMeasurementParser;
import edu.byu.nlp.data.streams.IndexerCalculator;

/**
 * 
 * @author plf1
 * 
 * Data streams are agnostic to the kinds of data inside of 
 * them. This class defines how the raw data stream  
 * interfaces with Dataset Instances.
 *
 */
public class DataStreamInstance {

  public static final String 
    ANNOTATOR="annotator", 
    ANNOTATION="annotation", 
    STARTTIME = "starttime", 
    ENDTIME = "endtime", 
    LABEL = "label", 
    SOURCE = "source", 
    DATA = "data",
    DATAPATH = "datapath",
    MEASUREMENT = "measurement";

  public static String getSource(Map<String,Object> instance){
    if (instance.containsKey(instance)){
      return (String) instance.get(SOURCE);
    }
    return null;
  }

  public static String getData(Map<String,Object> instance){
    if (instance.containsKey(instance)){
      return (String) instance.get(DATA);
    }
    return null;
  }

  public static String getDataPath(Map<String,Object> instance){
    if (instance.containsKey(instance)){
      return (String) instance.get(DATAPATH);
    }
    return null;
  }
  
  public static Long getEndTime(Map<String,Object> instance){
    if (instance.containsKey(instance)){
      return Long.parseLong((String) instance.get(ENDTIME));
    }
    return null;
  }

  public static Long getStartTime(Map<String,Object> instance){
    if (instance.containsKey(instance)){
      return Long.parseLong((String) instance.get(STARTTIME));
    }
    return null;
  }

  public static Integer getLabel(Map<String,Object> instance, IndexerCalculator<String,String> indexes){
    if (instance.containsKey(instance)){
      return indexes.getLabelIndexer().indexOf((String)instance.get(LABEL));
    }
    return null;
  }

  public static Integer getAnnotator(Map<String,Object> instance, IndexerCalculator<String,String> indexes){
    if (instance.containsKey(instance)){
      return indexes.getAnnotatorIdIndexer().indexOf(Long.parseLong((String) instance.get(ANNOTATOR)));
    }
    return null;
  }

  public static Measurement<Integer, Integer> getMeasurement(Map<String,Object> instance, IndexerCalculator<String,String> indexes){
    if (instance.containsKey(instance)){
      long annotatorId = indexes.getAnnotatorIdIndexer().get(getAnnotator(instance, indexes));
      return ClassificationMeasurementParser.parse((String)instance.get(MEASUREMENT), annotatorId, indexes);
    }
    return null;
  }

  public static Integer getAnnotation(Map<String,Object> instance, IndexerCalculator<String,String> indexes){
    if (instance.containsKey(instance)){
      return indexes.getLabelIndexer().indexOf((String) instance.get(ANNOTATION));
    }
    return null;
  }
  
}
