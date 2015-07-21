package edu.byu.nlp.data;

import java.util.Map;

import edu.byu.nlp.data.types.DataStreamInstance;
import edu.byu.nlp.data.types.DatasetInstance;
import edu.byu.nlp.data.types.Measurement;
import edu.byu.nlp.data.types.SparseFeatureVector;

public class FlatClassificationInstance extends AbstractFlatInstance<SparseFeatureVector, Integer> {

  private Map<String, Object> instance;

  private FlatClassificationInstance(Map<String,Object> raw){
    this.instance=raw;
  }

  public static FlatClassificationInstance fromDatasetInstance(DatasetInstance inst){
    return fromStream(DataStreamInstance.fromLabel(inst));
  }
  
  public static FlatClassificationInstance fromStream(Map<String,Object> raw){
    return new FlatClassificationInstance(raw);
  }
  
  public Map<String,Object> asStream(){
    return instance;
  }
  
  @Override
  public SparseFeatureVector getData() {
    return (SparseFeatureVector) DataStreamInstance.getData(instance);
  }

  @Override
  public Integer getLabel() {
    return (Integer) DataStreamInstance.getLabel(instance);
  }

  @Override
  public Integer getAnnotation() {
    return (Integer) DataStreamInstance.getAnnotation(instance);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Measurement<Integer> getMeasurement() {
    return (Measurement<Integer>) DataStreamInstance.getMeasurement(instance);
  }

  @Override
  public boolean isAnnotation() {
    return DataStreamInstance.isAnnotation(instance);
  }
  
  @Override
  public boolean isMeasurement() {
    return DataStreamInstance.isMeasurement(instance);
  }

  @Override
  public boolean isLabel() {
    return DataStreamInstance.isLabel(instance);
  }

  @Override
  public boolean isLabelObserved() {
    Boolean rawval = (Boolean) DataStreamInstance.getLabelObserved(instance);
    return rawval!=null && (boolean) rawval;
  }

  @Override
  public long getStartTimestamp() {
    return (long) DataStreamInstance.getStartTime(instance);
  }

  @Override
  public long getEndTimestamp() {
    return (long) DataStreamInstance.getEndTime(instance);
  }

  @Override
  public int getAnnotator() {
    Object rawAnnotator = DataStreamInstance.getAnnotator(instance);
    if (rawAnnotator!=null){
      return (int) rawAnnotator;
    }
    return -1;
  }

  @Override
  public int getInstanceId() {
    return (int) DataStreamInstance.getSource(instance);
  }

  @Override
  public String getSource() {
    return ""+getInstanceId();
  }


}
