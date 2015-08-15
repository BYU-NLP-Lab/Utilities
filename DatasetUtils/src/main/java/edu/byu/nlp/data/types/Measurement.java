package edu.byu.nlp.data.types;


/**
 * A Measurement generalizes the concept of a label or annotation.
 * It is based on Percy Liang's paper:
 * - "Learning from Measurements in Exponential Families." 
 * - http://www.cs.berkeley.edu/~jordan/papers/liang-jordan-klein-icml09.pdf
 * 
 * This class does not attempt to implement measurement functionality 
 * (implementing/calculating measurement functions over a dataset). 
 * It is meant to encode an observed measurement provided by a human.
 * 
 * Each type of measurement shares some common information (annotator, value, confidence)
 * Type-specific information is encoded in sub-interfaces. 
 */
public interface Measurement extends Comparable<Measurement> {

  int getAnnotator();
  
  double getValue();
  
  double getConfidence();
  
  /**
   * The moment at which the measurement was first asked of the annotator.
   */
  long getStartTimestamp();

  /**
   * The moment at which the measurement was finished.
   */
  long getEndTimestamp();
  
}
