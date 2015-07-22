package edu.byu.nlp.data;

import java.util.Map;

import edu.byu.nlp.data.types.DataStreamInstance;
import edu.byu.nlp.data.types.DatasetInstance;
import edu.byu.nlp.data.types.Measurement;
import edu.byu.nlp.data.types.SparseFeatureVector;

public class FlatInstances {

  private FlatInstances(){}

  public static FlatInstance<SparseFeatureVector, Integer> fromDatasetLabel(DatasetInstance instance){
    return fromStreamClassificationInstance(DataStreamInstance.fromLabel(instance));
  }
  
  @SuppressWarnings("unchecked")
  public static FlatInstance<SparseFeatureVector, Integer> fromStreamClassificationInstance(Map<String,Object> instance){
    return new BasicFlatInstance<SparseFeatureVector, Integer>(
        (Integer)DataStreamInstance.getSource(instance),
        (String)DataStreamInstance.getRawSource(instance),
        (SparseFeatureVector)DataStreamInstance.getData(instance), 
        (Integer)DataStreamInstance.getLabel(instance), 
        (Boolean)DataStreamInstance.getLabelObserved(instance), 
        (Integer)DataStreamInstance.getAnnotator(instance), 
        (Integer)DataStreamInstance.getAnnotation(instance), 
        (Measurement<Integer>)DataStreamInstance.getMeasurement(instance), 
        (Long)DataStreamInstance.getStartTime(instance), 
        (Long)DataStreamInstance.getEndTime(instance)
        );
  }
  
}
