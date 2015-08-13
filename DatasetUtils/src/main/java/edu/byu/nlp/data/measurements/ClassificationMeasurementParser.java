package edu.byu.nlp.data.measurements;

import edu.byu.nlp.data.measurements.ClassificationMeasurements.BasicClassificationAnnotationMeasurement;
import edu.byu.nlp.data.measurements.ClassificationMeasurements.BasicClassificationLabelProportionMeasurement;
import edu.byu.nlp.data.measurements.ClassificationMeasurements.BasicClassificationLabeledPredicateMeasurement;
import edu.byu.nlp.data.streams.IndexerCalculator;
import edu.byu.nlp.data.streams.JSONFileToAnnotatedDocumentList.MeasurementPojo;
import edu.byu.nlp.data.types.Measurement;

public class ClassificationMeasurementParser {

  public static final String TYPE = "type";

  public static Measurement pojoToMeasurement(MeasurementPojo pojo, String annotator, String source, long startTimestamp, long endTimestamp, IndexerCalculator<String,String> indexes){

    int annotatorId = indexes.getAnnotatorIdIndexer().indexOf(annotator);
    int labelIndex = indexes.getLabelIndexer().indexOf(pojo.label);

    // classification annotation
    if (pojo.type.equals("cls_ann")){
      return new BasicClassificationAnnotationMeasurement(annotatorId, pojo.value, pojo.confidence, source, labelIndex, startTimestamp, endTimestamp);
    }

    // classification label proportion 
    else if (pojo.type.equals("cls_prop")){
      return new BasicClassificationLabelProportionMeasurement(annotatorId, pojo.value, pojo.confidence, labelIndex, startTimestamp, endTimestamp);
    }

    // classification labeled predicate 
    else if (pojo.type.equals("cls_pred")){
      return new BasicClassificationLabeledPredicateMeasurement(annotatorId, pojo.value, pojo.confidence, labelIndex, pojo.predicate, startTimestamp, endTimestamp);
    }
    
    else{
      throw new IllegalArgumentException("unknown measurement type: "+pojo.type);
    }
    
  }
  
  

}
