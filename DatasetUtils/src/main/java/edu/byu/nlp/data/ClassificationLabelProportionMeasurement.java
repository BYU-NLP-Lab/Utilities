package edu.byu.nlp.data;


public class ClassificationLabelProportionMeasurement extends AbstractMeasurement<Integer>{

  private int label;

  /**
   * Represents a subjective human annotation judgment of 
   * how prevalent a given label will be. 
   * 
   */
  public ClassificationLabelProportionMeasurement(long annotator, int label){
    super(annotator, null);
    this.label=label;
  }
  
  @Override
  public double getValue(Integer index, Integer label) {
    return (label==this.label)? 1: 0;
  }

}
