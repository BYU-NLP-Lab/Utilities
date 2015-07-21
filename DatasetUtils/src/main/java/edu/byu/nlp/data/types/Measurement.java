package edu.byu.nlp.data.types;

/**
 * A Measurement generalizes the concept of a label or annotation.
 * It is based on Percy Liang's paper:
 * - "Learning from Measurements in Exponential Families." 
 * - http://www.cs.berkeley.edu/~jordan/papers/liang-jordan-klein-icml09.pdf
 */
public interface Measurement<Y> {

  /**
   * Measurements are defined with respect to a given dataset.
   * (e.g., they might reference a specific instance inside of a dataset). 
   */
  Dataset getDataset();
  
  /**
   * Returns the value of the Measurement on a 
   * single item. Mathematically, Measurments are 
   * defined in terms of parameters (x,y) representing 
   * (possibly structured) data instance X and 
   * (possibly structured) hypothesis label Y. 
   * 
   * Measurements encode all sorts of specific, fine-grained human 
   * judgments. For example, one kind of measurement 
   * encodes a specific label for a specific data item i by returning 
   * 1(x=x_i, y=y_i) based on prior knowledge of x_i and y_i. 
   * 
   * Measurements represent specific human judgments and should 
   * be similarly immutable (changed only if a human changes 
   * their judgment). They are not mutated as a part of 
   * algorithmic computation. 
   */
  double featureValue(int docIndex, Y label);
  
  /**
   * Who was responsible for generating this measurement? 
   */
  int getAnnotator();
  
}
