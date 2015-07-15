package edu.byu.nlp.data;

import static org.junit.Assert.*;

import org.junit.Test;

public class ClassificationAnnotationMeasurementTest {

  private static final double THRESHOLD = 1e-20;
  
  @Test
  public void test() {
    int index = 9;
    int label = 1;
    double annotation = 0.9;
    ClassificationAnnotationMeasurement m = new ClassificationAnnotationMeasurement(index, label, annotation );

    for (int i=0; i<20; i++){
      for (int j=0; j<20; j++){
        if (i==index && j==label){
          assertEquals(annotation, m.getValue(i, j), THRESHOLD);
        }
        else{
          assertEquals(0, m.getValue(i, j), THRESHOLD);
        }
      }
    }

  }

}
