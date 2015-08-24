package edu.byu.nlp.data.measurements;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ComparisonChain;

import edu.byu.nlp.data.streams.JSONFileToAnnotatedDocumentList.MeasurementPojo;
import edu.byu.nlp.data.types.Measurement;
import edu.byu.nlp.util.DoubleArrays;

public class ClassificationMeasurements {

  public interface ClassificationMeasurement extends Measurement{
    int getLabel();
  }
  
  public interface ClassificationAnnotationMeasurement extends ClassificationMeasurement{
    String getDocumentSource();
    double getValue();
  }

  public interface ClassificationLabelProportionMeasurement extends ClassificationMeasurement{
  }

  public interface ClassificationRelativeProportionMeasurement{
    int getLabel1();
    int getLabel2();
  }
  
  public interface ClassificationLabeledPredicateMeasurement extends ClassificationMeasurement{
    /**
     * returns the predicate this measurement uses to match documents 
     * (document source is passed in via the generic String parameter)
     */
    String getPredicate();
  }
  
  public interface ClassificationLabeledLocationMeasurement extends ClassificationMeasurement{
    double[] getLocation();
    String getSource();
    int getNeighbors();
  }
  
  
  public static abstract class AbstractMeasurement implements Measurement {

    private int annotator;
    private double value, confidence;
    private long startTimestamp, endTimestamp;
    private MeasurementPojo pojo;

    public AbstractMeasurement(MeasurementPojo pojo, int annotator, double value, double confidence, long startTimestamp, long endTimestamp){
      this.pojo=pojo;
      this.annotator=annotator;
      this.value=value;
      this.confidence=confidence;
      this.startTimestamp=startTimestamp;
      this.endTimestamp=endTimestamp;
    }
    
    public MeasurementPojo getPojo() {
      return pojo;
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
    @Override
    public long getStartTimestamp() {
      return startTimestamp;
    }
    @Override
    public long getEndTimestamp() {
      return endTimestamp;
    }
    @Override
    public String toString() {
      return MoreObjects.toStringHelper(ClassificationMeasurement.class)
          .add("annotator", getAnnotator())
          .add("value", getValue())
          .add("confidence", getConfidence())
          .toString();
    }
    // let equality be determined just by object ref
    @Override
    public final int hashCode() {
      return super.hashCode();
    }
    @Override
    public final boolean equals(Object obj) {
      return super.equals(obj);
    }
    @Override
    public int compareTo(Measurement o) {
      return ComparisonChain.start()
          .compare(getAnnotator(), o.getAnnotator())
          .compare(getConfidence(), o.getConfidence())
          .compare(getValue(), o.getValue())
          .compare(getEndTimestamp(), o.getEndTimestamp())
          .compare(getStartTimestamp(), o.getStartTimestamp())
          .compare(getClass().getName(), o.getClass().getName())
          .result();
    }
  }
  

  public static abstract class AbstractClassificationMeasurement extends AbstractMeasurement implements ClassificationMeasurement {
    private int label;
    public AbstractClassificationMeasurement(MeasurementPojo pojo, int annotator, double value, double confidence, int label, long startTimestamp, long endTimestamp){
      super(pojo, annotator, value, confidence, startTimestamp, endTimestamp);
      this.label=label;
    }
    @Override
    public int getLabel() {
      return label;
    }
    @Override
    public int compareTo(Measurement o) {
      if (super.compareTo(o)!=0){
        return super.compareTo(o);
      }
      return ComparisonChain.start()
          .compare(getLabel(), ((ClassificationMeasurement)o).getLabel())
          .result();
    }
    @Override
    public String toString() {
      return MoreObjects.toStringHelper(ClassificationMeasurement.class)
          .add("annotator", getAnnotator())
          .add("value", getValue())
          .add("confidence", getConfidence())
          .add("label", getLabel())
          .toString();
    }
  }
  
  
  /**
   * Represents a subjective human annotation judgment of a hypothesis label. 
   * The annotation itself is a binary judgment encoded as a double. Usually it 
   * will be 1 (the label is correct), or 0 (the label is incorrect), but it 
   * could be anywhere in the range. 
   * @param source 
   * 
   */
  public static class BasicClassificationAnnotationMeasurement extends AbstractClassificationMeasurement implements ClassificationAnnotationMeasurement{

    private String source;

    public BasicClassificationAnnotationMeasurement(int annotator, double value, double confidence, String source, int label, long startTimestamp, long endTimestamp){
      this(null,annotator,value,confidence,source,label,startTimestamp,endTimestamp);
    }
    public BasicClassificationAnnotationMeasurement(MeasurementPojo pojo, int annotator, double value, double confidence, String source, int label, long startTimestamp, long endTimestamp){
      super(pojo, annotator, value, confidence, label, startTimestamp, endTimestamp);
      this.source=source;
    }
    @Override
    public String getDocumentSource() {
      return source;
    }
    @Override
    public int compareTo(Measurement o) {
      if (super.compareTo(o)!=0){
        return super.compareTo(o);
      }
      return ComparisonChain.start()
          .compare(getDocumentSource(), ((ClassificationAnnotationMeasurement)o).getDocumentSource())
          .result();
    }
    @Override
    public String toString() {
      return MoreObjects.toStringHelper(ClassificationAnnotationMeasurement.class)
          .add("annotator", getAnnotator())
          .add("value", getValue())
          .add("confidence", getConfidence())
          .add("label", getLabel())
          .add("source", getDocumentSource())
          .toString();
    }
  }
  
  
  
  public static class BasicClassificationLabelProportionMeasurement extends AbstractClassificationMeasurement implements ClassificationLabelProportionMeasurement{
    public BasicClassificationLabelProportionMeasurement(MeasurementPojo pojo, int annotator, double value, double confidence, int label, long startTimestamp, long endTimestamp){
      super(pojo, annotator, value, confidence, label, startTimestamp, endTimestamp);
    }
    @Override
    public String toString() {
      return MoreObjects.toStringHelper(ClassificationLabelProportionMeasurement.class)
          .add("annotator", getAnnotator())
          .add("value", getValue())
          .add("confidence", getConfidence())
          .add("label", getLabel())
          .toString();
    }
  }


  /**
   * A Labeled Predicate associates a given feature (a predicate fires on give data) 
   * with a particular label. The predicate is encoded as a string.
   */
  public static class BasicClassificationLabeledPredicateMeasurement extends AbstractClassificationMeasurement implements ClassificationLabeledPredicateMeasurement{

    private String predicate;
    public BasicClassificationLabeledPredicateMeasurement(MeasurementPojo pojo, int annotator, double value, double confidence, int label, String predicate, long startTimestamp, long endTimestamp){
      super(pojo, annotator, value, confidence, label, startTimestamp, endTimestamp);
      this.predicate=predicate;
    }
    @Override
    public String getPredicate() {
      return predicate;
    }
    @Override
    public int compareTo(Measurement o) {
      if (super.compareTo(o)!=0){
        return super.compareTo(o);
      }
      return ComparisonChain.start()
          .compare(getPredicate(), ((ClassificationLabeledPredicateMeasurement)o).getPredicate())
          .result();
    }
    @Override
    public String toString() {
      return MoreObjects.toStringHelper(ClassificationLabeledPredicateMeasurement.class)
          .add("annotator", getAnnotator())
          .add("value", getValue())
          .add("confidence", getConfidence())
          .add("label", getLabel())
          .add("predicate", getPredicate())
          .toString();
    }
    
  }
  

  /**
   * A Labeled Predicate associates a given feature (a predicate fires on give data) 
   * with a particular label. The predicate is encoded as a string.
   * The location can either be transmitted via a double array (location) or 
   * via source, assuming that the location of the source referred to is in the dataset 
   * that this measurement exists inside of.
   */
  public static class BasicClassificationLabeledLocationMeasurement extends AbstractClassificationMeasurement implements ClassificationLabeledLocationMeasurement{

    private double[] location;
    private String source;
    private int neighbors;

    public BasicClassificationLabeledLocationMeasurement(MeasurementPojo pojo, int annotator, double value, double confidence,
        int label, double[] location, String source, int neighbors, long startTimestamp, long endTimestamp) {
      super(pojo, annotator, value, confidence, label, startTimestamp, endTimestamp);
      this.location=location;
      this.source=source;
      this.neighbors=neighbors;
    }
    @Override
    public double[] getLocation() {
      return location;
    }
    @Override
    public String toString() {
      return MoreObjects.toStringHelper(ClassificationLabeledLocationMeasurement.class)
          .add("annotator", getAnnotator())
          .add("value", getValue())
          .add("confidence", getConfidence())
          .add("label", getLabel())
          .add("neighbors", neighbors)
          .add("location", DoubleArrays.toString(getLocation()))
          .toString();
    }
    @Override
    public int compareTo(Measurement o) {
      if (super.compareTo(o)!=0){
        return super.compareTo(o);
      }
      return DoubleArrays.compareTo(getLocation(), ((ClassificationLabeledLocationMeasurement)o).getLocation());
    }
    @Override
    public String getSource() {
      return source;
    }
    @Override
    public int getNeighbors() {
      return neighbors;
    }

  }
  
}
