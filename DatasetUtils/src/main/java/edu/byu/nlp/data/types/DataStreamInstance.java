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
    RAW_SOURCE = "rawsource",
    DATA = "data",
    DATAPATH = "datapath",
    MEASUREMENT = "measurement";

  public static Object getSource(Map<String,Object> instance){
    return getRaw(instance, SOURCE);
  }

  public static Object getRawSource(Map<String,Object> instance){
    return getRaw(instance, RAW_SOURCE);
  }

  public static Object getData(Map<String,Object> instance){
    return getRaw(instance, DATA);
  }

  public static Object getDataPath(Map<String,Object> instance){
    return getRaw(instance, DATAPATH);
  }
  
  public static Object getEndTime(Map<String,Object> instance){
    return getRaw(instance, ENDTIME);
  }

  public static Object getStartTime(Map<String,Object> instance){
    return getRaw(instance, STARTTIME);
  }

  public static Object getLabel(Map<String,Object> instance){
    return getRaw(instance, LABEL);
  }

  public static Object getAnnotator(Map<String,Object> instance){
    return getRaw(instance, ANNOTATOR);
  }

  public static Object getMeasurement(Map<String,Object> instance){
    return getRaw(instance, MEASUREMENT);
  }

  public static Object getAnnotation(Map<String,Object> instance){
    return getRaw(instance, ANNOTATION);
  }

  public static Object getLabelObserved(Map<String,Object> instance){
    return getRaw(instance, LABELOBSERVED);
  }

  public static boolean isAnnotation(Map<String,Object> instance){
    return instance.containsKey(ANNOTATION);
  }
  
  public static boolean isLabel(Map<String, Object> instance) {
    return instance.containsKey(LABEL);
  }

  public static boolean isMeasurement(Map<String, Object> instance) {
    return instance.containsKey(MEASUREMENT);
  }
  

  public static Map<String, Object> fromFullRaw(Object source, Object rawSource, Object data, Object label, Object labelObserved, 
      Object datapath, Object annotator, Object annotation,  Object starttime, Object endtime, Object measurement){
    Map<String, Object> m = Maps.newHashMap();
    m.put(SOURCE, source);
    m.put(RAW_SOURCE, rawSource);
    addIfNotNull(m,ANNOTATOR,annotator);
    addIfNotNull(m,DATA,data);
    addIfNotNull(m,DATAPATH,datapath);
    addIfNotNull(m,ANNOTATION,annotation);
    addIfNotNull(m,LABEL,label);
    addIfNotNull(m,STARTTIME,starttime);
    addIfNotNull(m,ENDTIME,endtime);
    addIfNotNull(m,MEASUREMENT,measurement);
    addIfNotNull(m,LABELOBSERVED,labelObserved);
    return m;
  }
  
  public static Map<String, Object> fromFull(int source, String rawSource, SparseFeatureVector data, Integer label, Boolean labelObserved, 
      String datapath, Integer annotator, Integer annotation,  Long starttime, Long endtime, Measurement<Integer> measurement){
    return fromFullRaw(source, rawSource, data, label, labelObserved, datapath, annotator, annotation, starttime, endtime, measurement);
  }

  public static Map<String, Object> fromAnnotationRaw(Object source, Object rawSource, Object annotator, Object annotation, Object starttime, Object endtime, Object measurement){
    return fromFullRaw(source, rawSource, null, null, null, null, annotator, annotation, starttime, endtime, measurement);
  }

  public static Map<String, Object> fromAnnotation(int source, String rawSource, Integer annotator, Integer annotation,  Long starttime, Long endtime, Measurement<Integer> measurement){
    return fromAnnotationRaw(source, rawSource, annotator, annotation, starttime, endtime, measurement);
  }

  public static Map<String, Object> fromLabelRaw(Object source, Object rawSource, Object data, Object label, Object labelObserved){
    return fromFullRaw(source, rawSource, data, label, labelObserved, null, null, null, null, null, null);
  }
  
  public static Map<String, Object> fromLabel(int source, String rawSource, SparseFeatureVector data, Integer label, Boolean labelObserved){
    return fromLabelRaw(source, rawSource, data, label, labelObserved);
  }

  public static Map<String, Object> fromLabel(DatasetInstance inst){
    return fromLabel(inst.getInfo().getSource(), inst.getInfo().getRawSource(), inst.asFeatureVector(), inst.getLabel(), !DatasetInstances.isLabelConcealed(inst));
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
