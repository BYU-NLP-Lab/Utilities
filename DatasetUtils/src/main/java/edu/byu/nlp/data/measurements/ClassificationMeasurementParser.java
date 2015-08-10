package edu.byu.nlp.data.measurements;

import com.google.gson.Gson;

import edu.byu.nlp.data.measurements.ClassificationMeasurements.BasicClassificationAnnotationMeasurement;
import edu.byu.nlp.data.measurements.ClassificationMeasurements.BasicClassificationLabelProportionMeasurement;
import edu.byu.nlp.data.measurements.ClassificationMeasurements.BasicClassificationLabeledPredicateMeasurement;
import edu.byu.nlp.data.streams.IndexerCalculator;
import edu.byu.nlp.data.types.Measurement;

public class ClassificationMeasurementParser {

  public static final String TYPE = "type";

  public static Measurement stringToMeasurement(String rawValue, String annotator, String source, long startTimestamp, long endTimestamp, IndexerCalculator<String,String> indexes){
    return pojoToMeasurement(stringToPojo(rawValue), annotator, source, startTimestamp, endTimestamp, indexes);
  }

  public static MeasurementPojo stringToPojo(String rawValue){
    Gson gson = new Gson();
    return gson.fromJson(rawValue, MeasurementPojo.class);
  }
  
  public static Measurement pojoToMeasurement(MeasurementPojo pojo, String annotator, String source, long startTimestamp, long endTimestamp, IndexerCalculator<String,String> indexes){

    int annotatorId = indexes.getAnnotatorIdIndexer().indexOf(annotator);
    int labelIndex = indexes.getLabelIndexer().indexOf(pojo.label);

    // classification annotation
    if (pojo.type.equals("cls_ann")){
      return new BasicClassificationAnnotationMeasurement(annotatorId, pojo.value, pojo.confidence, source, labelIndex, startTimestamp, endTimestamp);
    }

    // classification label proportion 
    else if (pojo.type.equals("cls_lprop")){
      return new BasicClassificationLabelProportionMeasurement(annotatorId, pojo.value, pojo.confidence, labelIndex, startTimestamp, endTimestamp);
    }

    // classification labeled predicate 
    else if (pojo.type.equals("cls_lpred")){
      return new BasicClassificationLabeledPredicateMeasurement(annotatorId, pojo.value, pojo.confidence, labelIndex, pojo.predicate, startTimestamp, endTimestamp);
    }
    
    else{
      throw new IllegalArgumentException("unknown measurement type: "+pojo.type);
    }
    
  }
  
  
  public static class MeasurementPojo {
    private String type;
    private String label;
    private double value;
    private Double confidence;
    private String predicate;
  }

}
