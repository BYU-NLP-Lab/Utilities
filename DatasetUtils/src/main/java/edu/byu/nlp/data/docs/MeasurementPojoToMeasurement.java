package edu.byu.nlp.data.docs;

import java.util.Map;

import com.google.common.base.Function;

import edu.byu.nlp.data.measurements.ClassificationMeasurementParser;
import edu.byu.nlp.data.measurements.ClassificationMeasurementParser.MeasurementPojo;
import edu.byu.nlp.data.streams.IndexerCalculator;

public class MeasurementPojoToMeasurement implements Function<Map<String, Object>, Map<String, Object>> {

  private String measurementField;
  private String annotatorField;
  private String starttimeField;
  private String endtimeField;
  private IndexerCalculator<String, String> indexers;
  private String sourceField;

  public MeasurementPojoToMeasurement(String measurementField, String annotatorField, String sourceField, String starttimeField, String endtimeField, IndexerCalculator<String, String> indexers) {
    this.measurementField=measurementField;
    this.annotatorField=annotatorField;
    this.sourceField=sourceField;
    this.starttimeField=starttimeField;
    this.endtimeField=endtimeField;
    this.indexers=indexers;
  }

  @Override
  public Map<String, Object> apply(Map<String, Object> input) {
    if (input.containsKey(measurementField)){
      
      MeasurementPojo pojo = (MeasurementPojo) input.get(measurementField);
    
      long startTimestamp = (long) input.get(starttimeField);
      long endTimestamp = (long) input.get(endtimeField);
      String annotator = (String) input.get(annotatorField);
      String source = (String) input.get(sourceField);
      
      input.put(measurementField, 
          ClassificationMeasurementParser.pojoToMeasurement(pojo, annotator, source, startTimestamp, endTimestamp, indexers));
    }
    
    return input;
  }

}
