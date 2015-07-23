package edu.byu.nlp.data.measurements;

import java.util.regex.Matcher;

public class ClassificationMeasurements {

  public interface ClassificationAnnotationMeasurement{
    int getDocumentIndex();
    int getLabel();
  }

  public interface ClassificationProportionMeasurement{
    int getLabel();
  }

  public interface ClassificationRelativeProportionMeasurement{
    int getLabel1();
    int getLabel2();
  }
  
  public interface ClassificationLabeledPredicateMeasurement{
    int getLabel();
    Matcher getPredicate();
  }
  
}
