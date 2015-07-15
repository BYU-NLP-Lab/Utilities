package edu.byu.nlp.data;

import static org.junit.Assert.*;

import org.junit.Test;

public class ClassificationLabelProportionMeasurementTest {

  private static final double THRESHOLD = 1e-20;
  
  @Test
  public void test() {
    ClassificationLabelProportionMeasurement m = new ClassificationLabelProportionMeasurement(1);

    assertEquals(1, m.getValue(5, 1), THRESHOLD);
    assertEquals(1, m.getValue(0, 1), THRESHOLD);
    assertEquals(1, m.getValue(-1, 1), THRESHOLD);
    assertEquals(0, m.getValue(5, 4), THRESHOLD);
    assertEquals(0, m.getValue(-7, -2), THRESHOLD);
    assertEquals(0, m.getValue(2, 3), THRESHOLD);
  }

}
