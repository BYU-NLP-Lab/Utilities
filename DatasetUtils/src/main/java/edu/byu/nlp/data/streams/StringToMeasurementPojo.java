package edu.byu.nlp.data.streams;

import com.google.common.base.Function;

import edu.byu.nlp.data.measurements.ClassificationMeasurementParser;
import edu.byu.nlp.data.measurements.ClassificationMeasurementParser.MeasurementPojo;

public class StringToMeasurementPojo implements Function<String, MeasurementPojo> {

  @Override
  public MeasurementPojo apply(String input) {
    return ClassificationMeasurementParser.stringToPojo(input);
  }

}
