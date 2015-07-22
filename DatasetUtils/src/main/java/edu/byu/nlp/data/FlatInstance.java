package edu.byu.nlp.data;

import edu.byu.nlp.data.types.Measurement;

/**
 * 
 * @author plf1
 *
 *         This class acts as a wrapper over data stream 
 *         instances providing typed accessors for 
 *         common data types (after the streams have been fully 
 *         parsed and processed) 
 *
 */
public interface FlatInstance<D, L> {

  public static long NULL_TIMESTAMP = -1;
  public static long NULL_ID = -1;

  /**
   * The data associated with this instance (often a String or featurevector)
   */
  D getData();

  /**
   * Get the measurement value. Could be null if this is an annotation or label.
   */
  Measurement<L> getMeasurement();
  
  /**
   * Get the label value. Could be null if this is an annotation.
   */
  L getLabel();

  /**
   * Get the annotation value. Could be null if this is a label.
   */
  L getAnnotation();

  /**
   * Returns true if this instance represents an annotated instance; otherwise
   * false.
   */
  boolean isAnnotation();

  /**
   * Returns true if this instance represents a measurement; otherwise false
   */
  boolean isMeasurement();

  /**
   * Returns true if this instance represents a labeled instance; otherwise false
   */
  boolean isLabel();
  
  /**
   * Returns true if this instance has a label that was observed and publically 
   * known at annotation time. 
   */
  boolean isLabelObserved();

  /**
   * The moment at which the instance was first presented to the annotator.
   */
  Long getStartTimestamp();

  /**
   * The moment at which the instance annotation was finished.
   */
  Long getEndTimestamp();

  /**
   * The id of the annotator (automatic or otherwise) that produced either the
   * annotation or automatic value. For accepted gold-standard labels, this
   * value should be equal to Constants.GOLD_AUTOMATIC_ANNOTATOR_ID
   */
  Integer getAnnotator();

  /**
   * Returns a number that uniquely identifies the instance annotated or labeled
   * here.
   */
  int getInstanceId();

  /**
   * Returns a string representing the origin of this instance such as a url or
   * file path. Should be a human-readable stand-in for instanceId.
   */
  String getSource();


}
