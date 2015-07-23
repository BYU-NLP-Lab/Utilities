package edu.byu.nlp.data.measurements;




public class ClassificationLabelProportionMeasurement extends AbstractMeasurement{

  private int label;

  /**
   * Represents a subjective human annotation judgment of 
   * how prevalent a given label will be. 
   * 
   */
  public ClassificationLabelProportionMeasurement(int annotator, double value, double confidence, int label){
    super(annotator, value, confidence);
    this.label=label;
  }

  public int getLabel() {
    return label;
  }

}
