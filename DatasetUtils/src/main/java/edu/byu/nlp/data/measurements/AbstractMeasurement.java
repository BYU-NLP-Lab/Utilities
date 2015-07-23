package edu.byu.nlp.data.measurements;

import com.google.common.base.Preconditions;

import edu.byu.nlp.data.types.Measurement;

public abstract class AbstractMeasurement implements Measurement {

  private int annotator;
  private double value;
  private double confidence;

  public AbstractMeasurement(int annotator, double value, double confidence){
    Preconditions.checkArgument(-1 <= value && value <= 1, "'binary' annotation values must be between -1 and 1 (not "+value+")");
    this.annotator=annotator;
    this.value=value;
    this.confidence=confidence;
  }
  
  @Override
  public int getAnnotator() {
    return annotator;
  }
  
  @Override
  public double getValue() {
    return value;
  }
  
  @Override
  public double getConfidence() {
    return confidence;
  }
  
}

