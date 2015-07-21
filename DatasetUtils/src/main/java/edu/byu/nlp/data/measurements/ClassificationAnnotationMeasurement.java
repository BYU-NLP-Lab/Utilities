package edu.byu.nlp.data.measurements;

import com.google.common.base.Preconditions;

public class ClassificationAnnotationMeasurement extends AbstractMeasurement<Integer>{

  private int index;
  private double annotation;
  private int label;
  private Double confidence;

  /**
   * Represents a subjective human annotation judgment of a hypothesis label. 
   * The annotation itself is a binary judgment encoded as a double. Usually it 
   * will be 1 (the label is correct), or -1 (the label is incorrect), but it 
   * could be anywhere in the range. 
   * 
   */
  public ClassificationAnnotationMeasurement(int annotator, int index, int label, double annotation, Double confidence){
    super(annotator, null);
    Preconditions.checkArgument(-1 <= annotation && annotation <= 1, "'binary' annotation values must be between -1 and 1 (not "+annotation+")");
    this.setIndex(index);
    this.setLabel(label);
    this.setAnnotation(annotation);
    this.setConfidence(confidence);
  }
  
  @Override
  public double featureValue(int docIndex, Integer label) {
    return (docIndex==this.getIndex() && label==this.getLabel())? getAnnotation(): 0;
  }

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  public double getAnnotation() {
    return annotation;
  }

  public void setAnnotation(double annotation) {
    this.annotation = annotation;
  }

  public int getLabel() {
    return label;
  }

  public void setLabel(int label) {
    this.label = label;
  }

  public Double getConfidence() {
    return confidence;
  }

  public void setConfidence(Double confidence) {
    this.confidence = confidence;
  }


}
