package edu.byu.nlp.data;

import edu.byu.nlp.data.types.Measurement;

public class BasicFlatInstance<D,L> extends AbstractFlatInstance<D,L> {

  private int source;
  private String rawSource;
  private D data;
  private L label;
  private Integer annotator;
  private L annotation;
  private Measurement measurement;
  private Long starttime;
  private Long endtime;
  private Boolean labelObserved;

  public BasicFlatInstance(int source, String rawSource, 
      D data, L label, Boolean labelObserved, 
      Integer annotator, L annotation, Measurement measurement,
      Long starttime, Long endtime){
    this.source=source;
    this.rawSource=rawSource;
    this.data=data;
    this.label=label;
    this.labelObserved=labelObserved;
    this.annotator=annotator;
    this.annotation=annotation;
    this.measurement=measurement;
    this.starttime=starttime;
    this.endtime=endtime;
  }

  public BasicFlatInstance(int source, String rawSource, 
      D data, L label, Boolean labelObserved){
    this(source, rawSource, data, label, labelObserved, null, null, null, null, null);
  }

  public BasicFlatInstance(int source, String rawSource, 
      Integer annotator, L annotation, Measurement measurement,
      Long starttime, Long endtime){
    this(source, rawSource, null, null, null, annotator, annotation, measurement, starttime, endtime);
  }
  
  @Override
  public D getData() {
    return data;
  }

  @Override
  public Measurement getMeasurement() {
    return measurement;
  }

  @Override
  public L getLabel() {
    return label;
  }

  @Override
  public L getAnnotation() {
    return annotation;
  }

  @Override
  public boolean isAnnotation() {
    return getAnnotation()!=null;
  }

  @Override
  public boolean isMeasurement() {
    return getMeasurement()!=null;
  }

  @Override
  public boolean isLabel() {
    return getLabel()!=null;
  }

  @Override
  public boolean isLabelObserved() {
    return (labelObserved==null)? false: labelObserved; 
  }

  @Override
  public Long getStartTimestamp() {
    return starttime;
  }

  @Override
  public Long getEndTimestamp() {
    return endtime;
  }

  @Override
  public Integer getAnnotator() {
    return annotator;
  }

  @Override
  public int getInstanceId() {
    return source;
  }

  @Override
  public String getSource() {
    return rawSource;
  }

}
