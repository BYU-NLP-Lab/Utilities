package edu.byu.nlp.data.measurements;


public class ClassificationAnnotationMeasurement extends AbstractMeasurement{

  private int index;
  private int label;

  /**
   * Represents a subjective human annotation judgment of a hypothesis label. 
   * The annotation itself is a binary judgment encoded as a double. Usually it 
   * will be 1 (the label is correct), or -1 (the label is incorrect), but it 
   * could be anywhere in the range. 
   * 
   */
  public ClassificationAnnotationMeasurement(int annotator, double value, Double confidence, int index, int label){
    super(annotator, value, confidence);
    this.index=index;
    this.label=label;
  }

  public int getIndex() {
    return index;
  }

  public int getLabel() {
    return label;
  }

  public void setLabel(int label) {
    this.label = label;
  }

}
