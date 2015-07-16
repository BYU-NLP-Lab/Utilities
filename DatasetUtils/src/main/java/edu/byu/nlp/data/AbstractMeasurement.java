package edu.byu.nlp.data;

import edu.byu.nlp.data.types.Dataset;
import edu.byu.nlp.data.types.Measurement;

/**
 * 
 * @author plf1
 *
 * A reference to a dataset is formed and instances are assumed to 
 * be defined by an <Integer> index reference.  
 */
public abstract class AbstractMeasurement<Y> implements Measurement<Integer, Y>{

  private Dataset dataset;
  private long annotator;

  public AbstractMeasurement(long annotator, Dataset dataset){
    this.annotator=annotator;
    this.dataset=dataset;
  }
  
  @Override
  public long getAnnotator() {
    return annotator;
  }

  @Override
  public Dataset getDataset() {
    return dataset;
  }

}
