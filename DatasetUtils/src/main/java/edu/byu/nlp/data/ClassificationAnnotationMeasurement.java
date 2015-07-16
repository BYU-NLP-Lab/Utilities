package edu.byu.nlp.data;

import com.google.common.base.Preconditions;

public class ClassificationAnnotationMeasurement extends AbstractMeasurement<Integer>{

  private int index;
  private double annotation;
  private int label;

  /**
   * Represents a subjective human annotation judgment of a hypothesis label. 
   * The annotation itself is a binary judgment encoded as a double. Usually it 
   * will be 1 (the label is correct), or -1 (the label is incorrect), but it 
   * could be anywhere in the range. 
   * 
   */
  public ClassificationAnnotationMeasurement(int annotator, int index, int label, double annotation){
    super(annotator, null);
    Preconditions.checkArgument(-1 <= annotation && annotation <= 1, "'binary' annotation values must be between -1 and 1 (not "+annotation+")");
    this.index=index;
    this.label=label;
    this.annotation=annotation;
  }
  
  @Override
  public double getValue(Integer index, Integer label) {
    return (index==this.index && label==this.label)? annotation: 0;
  }

}
