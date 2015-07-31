package edu.byu.nlp.data.measurements;

import java.util.regex.Matcher;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

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
    Matcher getPredicate();
  }
  
  
  
  public static abstract class AbstractMeasurement implements Measurement {

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
    @Override
    public String toString() {
      return MoreObjects.toStringHelper(ClassificationMeasurement.class)
          .add("annotator", getAnnotator())
          .add("value", getValue())
          .add("confidence", getConfidence())
          .toString();
    }
  }
  

  public static abstract class AbstractClassificationMeasurement extends AbstractMeasurement implements ClassificationMeasurement {
    private int label;
    public AbstractClassificationMeasurement(int annotator, double value, double confidence, int label){
      super(annotator, value, confidence);
      this.label=label;
    }
    @Override
    public int getLabel() {
      return label;
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
   * will be 1 (the label is correct), or -1 (the label is incorrect), but it 
   * could be anywhere in the range. 
   * @param source 
   * 
   */
  public static class BasicClassificationAnnotationMeasurement extends AbstractClassificationMeasurement implements ClassificationAnnotationMeasurement{

    private String source;

    public BasicClassificationAnnotationMeasurement(int annotator, double value, Double confidence, String source, int label){
      super(annotator, value, confidence, label);
      this.source=source;
    }

    @Override
    public String getDocumentSource() {
      return source;
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
    public BasicClassificationLabelProportionMeasurement(int annotator, double value, double confidence, int label){
      super(annotator, value, confidence, label);
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
  
  
}
