package edu.byu.nlp.data.measurements;

import java.util.regex.Matcher;

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
    
  }
  
  
  
  public static class BasicClassificationAnnotationMeasurement extends AbstractMeasurement implements ClassificationAnnotationMeasurement{

    private int label;
    private String source;

    /**
     * Represents a subjective human annotation judgment of a hypothesis label. 
     * The annotation itself is a binary judgment encoded as a double. Usually it 
     * will be 1 (the label is correct), or -1 (the label is incorrect), but it 
     * could be anywhere in the range. 
     * @param source 
     * 
     */
    public BasicClassificationAnnotationMeasurement(int annotator, double value, Double confidence, String source, int label){
      super(annotator, value, confidence);
      this.source=source;
      this.label=label;
    }

    @Override
    public String getDocumentSource() {
      return source;
    }

    public int getLabel() {
      return label;
    }

    public void setLabel(int label) {
      this.label = label;
    }

  }
  
  
  
  public static class BasicClassificationLabelProportionMeasurement extends AbstractMeasurement implements ClassificationProportionMeasurement{

    private int label;

    /**
     * Represents a subjective human annotation judgment of 
     * how prevalent a given label will be. 
     * 
     */
    public BasicClassificationLabelProportionMeasurement(int annotator, double value, double confidence, int label){
      super(annotator, value, confidence);
      this.label=label;
    }

    public int getLabel() {
      return label;
    }

  }
  
  
}
