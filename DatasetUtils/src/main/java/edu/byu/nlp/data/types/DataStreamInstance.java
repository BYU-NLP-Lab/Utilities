package edu.byu.nlp.data.types;

import java.util.Map;

import com.google.common.collect.Maps;

import edu.byu.nlp.dataset.DatasetInstances;

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
    LABELOBSERVED = "labelobserved",
    SOURCE = "source", 
    DATA = "data",
    DATAPATH = "datapath",
    MEASUREMENT = "measurement";

  public static Integer getSource(Map<String,Object> instance){
    return (Integer) getRaw(instance, SOURCE);
  }

  public static SparseFeatureVector getData(Map<String,Object> instance){
    return (SparseFeatureVector) getRaw(instance, DATA);
  }

  public static String getDataPath(Map<String,Object> instance){
    return (String) getRaw(instance, DATAPATH);
  }
  
  public static Long getEndTime(Map<String,Object> instance){
    return (Long) getRaw(instance, ENDTIME);
  }

  public static Long getStartTime(Map<String,Object> instance){
    return (Long) getRaw(instance, STARTTIME);
  }

  public static Integer getLabel(Map<String,Object> instance){
    return (Integer) getRaw(instance, LABEL);
  }

  public static Integer getAnnotator(Map<String,Object> instance){
    return (Integer) getRaw(instance, ANNOTATOR);
  }

  @SuppressWarnings("unchecked")
  public static Measurement<Integer, Integer> getMeasurement(Map<String,Object> instance){
    return (Measurement<Integer, Integer>) getRaw(instance, MEASUREMENT);
  }

  public static Integer getAnnotation(Map<String,Object> instance){
    return (Integer) getRaw(instance, ANNOTATION);
  }

  public static Boolean getLabelObserved(Map<String,Object> instance){
    return (Boolean) getRaw(instance, LABELOBSERVED);
  }

  public static boolean isAnnotation(Map<String,Object> instance){
    return instance.containsKey(ANNOTATION);
  }

  public static Map<String, Object> fromFullRaw(Object source, Object data, Object label, Object labelObserved, 
      Object datapath, Object annotator, Object annotation,  Object starttime, Object endtime, Object measurement){
    Map<String, Object> m = Maps.newHashMap();
    m.put(SOURCE, source);
    addIfNotNull(m,ANNOTATOR,annotator);
    addIfNotNull(m,DATA,data);
    addIfNotNull(m,DATAPATH,datapath);
    addIfNotNull(m,ANNOTATION,annotation);
    addIfNotNull(m,LABEL,label);
    addIfNotNull(m,STARTTIME,starttime);
    addIfNotNull(m,ENDTIME,endtime);
    addIfNotNull(m,MEASUREMENT,measurement);
    addIfNotNull(m, LABELOBSERVED, labelObserved);
    return m;
  }
  
  public static Map<String, Object> fromFull(int source, SparseFeatureVector data, Integer label, Boolean labelObserved, 
      String datapath, Integer annotator, Integer annotation,  Long starttime, Long endtime, Measurement<Integer, Integer> measurement){
    return fromFullRaw(source, data, label, labelObserved, datapath, annotator, annotation, starttime, endtime, measurement);
  }

  public static Map<String, Object> fromAnnotationRaw(Object source, Object annotator, Object annotation, Object starttime, Object endtime, Object measurement){
    return fromFullRaw(source, null, null, null, null, annotator, annotation, starttime, endtime, measurement);
  }

  public static Map<String, Object> fromAnnotation(int source, Integer annotator, Integer annotation,  Long starttime, Long endtime, Measurement<Integer, Integer> measurement){
    return fromAnnotationRaw(source, annotator, annotation, starttime, endtime, measurement);
  }

  public static Map<String, Object> fromLabelRaw(Object source, Object data, Object label, Object labelObserved){
    return fromFullRaw(source, data, labelObserved, labelObserved, null, null, null, null, null, null);
  }
  
  public static Map<String, Object> fromLabel(int source, SparseFeatureVector data, Integer label, Boolean labelObserved){
    return fromLabelRaw(source, data, label, labelObserved);
  }

  public static Map<String, Object> fromLabel(DatasetInstance inst){
    return fromLabel(inst.getInfo().getSource(), inst.asFeatureVector(), inst.getLabel(), !DatasetInstances.isLabelConcealed(inst));
  }

  public static Object getRaw(Map<String, Object> m, String key){
    if (m.containsKey(key)){
      return m.get(key);
    }
    return null;
  }
  
  public static void addIfNotNull(Map<String, Object> m, String key, Object value){
    if (value !=null){
      m.put(key, value);
    }
  }
  
}
