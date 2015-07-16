package edu.byu.nlp.data.measurements;



public class ClassificationLabelProportionMeasurement extends AbstractMeasurement<Integer>{

  private int label;
  private double proportion;
  private Double confidence;

  /**
   * Represents a subjective human annotation judgment of 
   * how prevalent a given label will be. 
   * 
   */
  public ClassificationLabelProportionMeasurement(int annotator, int label, double proportion, Double confidence){
    super(annotator, null);
    this.setLabel(label);
    this.setProportion(proportion);
    this.setConfidence(confidence);
  }
  
  @Override
  public double featureValue(Integer index, Integer label) {
    return (label==this.getLabel())? 1: 0;
  }

  public double getProportion() {
    return proportion;
  }

  public void setProportion(double proportion) {
    this.proportion = proportion;
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
