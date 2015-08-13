package edu.byu.nlp.data.measurements;

import java.util.Objects;

import com.google.common.base.MoreObjects;

import edu.byu.nlp.data.types.Measurement;

public class ClassificationMeasurements {

  public interface ClassificationMeasurement{
    int getLabel();
  }
  
  public interface ClassificationAnnotationMeasurement extends ClassificationMeasurement{
    String getDocumentSource();
    double getValue();
  }

  public interface ClassificationProportionMeasurement extends ClassificationMeasurement{
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
  
  
  
  public static abstract class AbstractMeasurement implements Measurement {

    private int annotator;
    private double value, confidence;
    private long startTimestamp, endTimestamp;

    public AbstractMeasurement(int annotator, double value, double confidence, long startTimestamp, long endTimestamp){
      this.annotator=annotator;
      this.value=value;
      this.confidence=confidence;
      this.startTimestamp=startTimestamp;
      this.endTimestamp=endTimestamp;
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
    @Override
    public int hashCode() {
      return Objects.hash(annotator,value,confidence,startTimestamp,endTimestamp);
    }
    @Override
    public boolean equals(Object obj) {
      if (obj==null || !(obj instanceof Measurement)){
        return false;
      }
      Measurement other = (Measurement) obj;
      return annotator == other.getAnnotator()
          && value == other.getValue()
          && confidence == other.getConfidence()
          && startTimestamp == other.getStartTimestamp()
          && endTimestamp == other.getEndTimestamp()
      ;
    }
  }
  

  public static abstract class AbstractClassificationMeasurement extends AbstractMeasurement implements ClassificationMeasurement {
    private int label;
    public AbstractClassificationMeasurement(int annotator, double value, double confidence, int label, long startTimestamp, long endTimestamp){
      super(annotator, value, confidence, startTimestamp, endTimestamp);
      this.label=label;
    }
    @Override
    public int getLabel() {
      return label;
    }
    @Override
    public int hashCode() {
      return Objects.hash(super.hashCode(), label);
    }
    @Override
    public boolean equals(Object obj) {
      if (obj==null || !(obj instanceof ClassificationMeasurement)){
        return false;
      }
      return super.equals(obj)
          && Objects.equals(((ClassificationMeasurement)obj).getLabel(), label);
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
      super(annotator, value, confidence, label, startTimestamp, endTimestamp);
      this.source=source;
    }
    @Override
    public String getDocumentSource() {
      return source;
    }
    @Override
    public int hashCode() {
      return Objects.hash(super.hashCode(), source);
    }
    @Override
    public boolean equals(Object obj) {
      if (obj==null || !(obj instanceof ClassificationAnnotationMeasurement)){
        return false;
      }
      return super.equals(obj)
          && Objects.equals(((ClassificationAnnotationMeasurement)obj).getDocumentSource(), source);
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
  
  
  
  public static class BasicClassificationLabelProportionMeasurement extends AbstractClassificationMeasurement implements ClassificationProportionMeasurement{
    public BasicClassificationLabelProportionMeasurement(int annotator, double value, double confidence, int label, long startTimestamp, long endTimestamp){
      super(annotator, value, confidence, label, startTimestamp, endTimestamp);
    }
    @Override
    public String toString() {
      return MoreObjects.toStringHelper(ClassificationProportionMeasurement.class)
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
    public BasicClassificationLabeledPredicateMeasurement(int annotator, double value, double confidence, int label, String predicate, long startTimestamp, long endTimestamp){
      super(annotator, value, confidence, label, startTimestamp, endTimestamp);
      this.predicate=predicate;
    }
    @Override
    public int hashCode() {
      return Objects.hash(super.hashCode(), predicate);
    }
    @Override
    public boolean equals(Object obj) {
      if (obj==null || !(obj instanceof ClassificationLabeledPredicateMeasurement)){
        return false;
      }
      return super.equals(obj)
          && Objects.equals(((ClassificationLabeledPredicateMeasurement)obj).getPredicate(), predicate);
    }
    @Override
    public String getPredicate() {
      return predicate;
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
  
}
