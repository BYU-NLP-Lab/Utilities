package edu.byu.nlp.data.measurements;

import java.util.Set;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.google.common.collect.Sets;

import edu.byu.nlp.data.types.Measurement;


public class ClassificationMeasurementsTest {
  
  @Test
  public void testHashCode(){
    Set<Measurement> set;
    int annotator = 0;
    double value = 0;
    double confidence = 0;
    String source = null;
    int label = 0;
    long startTimestamp = 0;
    long endTimestamp = 0;

    // make sure equal measurements are merged
    set = Sets.newHashSet();
    set.add(new ClassificationMeasurements.BasicClassificationAnnotationMeasurement(
        annotator, value, confidence, source, label, startTimestamp, endTimestamp));
    set.add(new ClassificationMeasurements.BasicClassificationAnnotationMeasurement(
        annotator, value, confidence, source, label, startTimestamp, endTimestamp));
    Assertions.assertThat(set.size()).isEqualTo(1);
    

    // make sure unequal annotators are not merged
    set = Sets.newHashSet();
    set.add(new ClassificationMeasurements.BasicClassificationAnnotationMeasurement(
        annotator+1, value, confidence, source, label, startTimestamp, endTimestamp));
    set.add(new ClassificationMeasurements.BasicClassificationAnnotationMeasurement(
        annotator, value, confidence, source, label, startTimestamp, endTimestamp));
    Assertions.assertThat(set.size()).isEqualTo(2);

    // make sure unequal values are not merged
    set = Sets.newHashSet();
    set.add(new ClassificationMeasurements.BasicClassificationAnnotationMeasurement(
        annotator, value+1, confidence, source, label, startTimestamp, endTimestamp));
    set.add(new ClassificationMeasurements.BasicClassificationAnnotationMeasurement(
        annotator, value, confidence, source, label, startTimestamp, endTimestamp));
    Assertions.assertThat(set.size()).isEqualTo(2);

    // make sure unequal confidences are not merged
    set = Sets.newHashSet();
    set.add(new ClassificationMeasurements.BasicClassificationAnnotationMeasurement(
        annotator, value, confidence+1, source, label, startTimestamp, endTimestamp));
    set.add(new ClassificationMeasurements.BasicClassificationAnnotationMeasurement(
        annotator, value, confidence, source, label, startTimestamp, endTimestamp));
    Assertions.assertThat(set.size()).isEqualTo(2);

    // make sure unequal sources are not merged
    set = Sets.newHashSet();
    set.add(new ClassificationMeasurements.BasicClassificationAnnotationMeasurement(
        annotator, value, confidence, source+"asd", label, startTimestamp, endTimestamp));
    set.add(new ClassificationMeasurements.BasicClassificationAnnotationMeasurement(
        annotator, value, confidence, source, label, startTimestamp, endTimestamp));
    Assertions.assertThat(set.size()).isEqualTo(2);

    // make sure unequal labels are not merged
    set = Sets.newHashSet();
    set.add(new ClassificationMeasurements.BasicClassificationAnnotationMeasurement(
        annotator, value, confidence, source, label+1, startTimestamp, endTimestamp));
    set.add(new ClassificationMeasurements.BasicClassificationAnnotationMeasurement(
        annotator, value, confidence, source, label, startTimestamp, endTimestamp));
    Assertions.assertThat(set.size()).isEqualTo(2);

    // make sure unequal starttime are not merged
    set = Sets.newHashSet();
    set.add(new ClassificationMeasurements.BasicClassificationAnnotationMeasurement(
        annotator, value, confidence, source, label, startTimestamp+1, endTimestamp));
    set.add(new ClassificationMeasurements.BasicClassificationAnnotationMeasurement(
        annotator, value, confidence, source, label, startTimestamp, endTimestamp));
    Assertions.assertThat(set.size()).isEqualTo(2);
    
    // make sure unequal endtime are not merged
    set = Sets.newHashSet();
    set.add(new ClassificationMeasurements.BasicClassificationAnnotationMeasurement(
        annotator, value, confidence, source, label, startTimestamp, endTimestamp+1));
    set.add(new ClassificationMeasurements.BasicClassificationAnnotationMeasurement(
        annotator, value, confidence, source, label, startTimestamp, endTimestamp));
    Assertions.assertThat(set.size()).isEqualTo(2);
  }
  

}
