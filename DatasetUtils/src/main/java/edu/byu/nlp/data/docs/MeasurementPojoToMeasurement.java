package edu.byu.nlp.data.docs;

import java.util.Map;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

import edu.byu.nlp.data.measurements.ClassificationMeasurementParser;
import edu.byu.nlp.data.measurements.ClassificationMeasurements;
import edu.byu.nlp.data.streams.IndexerCalculator;
import edu.byu.nlp.data.streams.JSONFileToAnnotatedDocumentList.MeasurementPojo;
import edu.byu.nlp.data.types.DataStreamInstance;

public class MeasurementPojoToMeasurement implements Function<Map<String, Object>, Map<String, Object>> {

  private IndexerCalculator<String, String> indexers;

  public MeasurementPojoToMeasurement(IndexerCalculator<String, String> indexers) {
    this.indexers=indexers;
  }

  @Override
  public Map<String, Object> apply(Map<String, Object> input) {
    // sanity check. each stream item must be either a measurement or annotation--not both
    // (although below we will adapt annotations to also be measurements, if necessary)
    Preconditions.checkArgument(!(DataStreamInstance.isMeasurement(input) && DataStreamInstance.isAnnotation(input)));
    
    Long startTimestamp = (Long) DataStreamInstance.getStartTime(input);
    Long endTimestamp = (Long) DataStreamInstance.getEndTime(input);
    String annotator = (String) DataStreamInstance.getAnnotator(input);
    String source = (String) DataStreamInstance.getSource(input);

    // Create a measurement from an explicit measurement value
    if (DataStreamInstance.isMeasurement(input)){
      MeasurementPojo pojo = (MeasurementPojo) DataStreamInstance.getMeasurement(input);
      input.put(DataStreamInstance.MEASUREMENT, 
          ClassificationMeasurementParser.pojoToMeasurement(pojo, annotator, source, startTimestamp, endTimestamp, indexers));
    }
    // Create a measurement from an annotation value
    else if (DataStreamInstance.isAnnotation(input)){
      double value = 1, confidence = 1;
      String rawAnnotation = (String) DataStreamInstance.getAnnotation(input);
      int label = indexers.getLabelIndexer().indexOf(rawAnnotation);
      int annotatorId = indexers.getAnnotatorIdIndexer().indexOf(annotator);
      input.put(DataStreamInstance.MEASUREMENT, 
          new ClassificationMeasurements.BasicClassificationAnnotationMeasurement(annotatorId, value, confidence, source, label, startTimestamp, endTimestamp));
    }
    
    return input;
  }

}
