package edu.byu.nlp.data.measurements;

import com.google.gson.Gson;

import edu.byu.nlp.data.measurements.ClassificationMeasurementParser.MeasurementPojos.ClassificationAnnotationMeasurementPojo;
import edu.byu.nlp.data.measurements.ClassificationMeasurementParser.MeasurementPojos.ClassificationLabelProportionMeasurementPojo;
import edu.byu.nlp.data.measurements.ClassificationMeasurementParser.MeasurementPojos.MeasurementPojo;
import edu.byu.nlp.data.streams.IndexerCalculator;
import edu.byu.nlp.data.types.Measurement;

public class ClassificationMeasurementParser<X,Y> {

  public static final String TYPE = "type";
  
  
  public static Measurement<Integer,Integer> parse(String rawValue, int annotator, IndexerCalculator<String,String> indexes){
    Gson gson = new Gson();
    String type = gson.fromJson(rawValue, MeasurementPojo.class).type;
    
    // classification annotation
    if (type.equals("cls_ann")){
      ClassificationAnnotationMeasurementPojo pojo = gson.fromJson(rawValue, ClassificationAnnotationMeasurementPojo.class);
      int labelIndex = indexes.getLabelIndexer().indexOf(pojo.label);
      int instanceIndex = indexes.getInstanceIdIndexer().indexOf(pojo.source);
      return new ClassificationAnnotationMeasurement(annotator, instanceIndex, labelIndex, pojo.value, pojo.confidence); 
    }
    
    // classification label proportion 
    else if (type.equals("cls_lprp")){
      ClassificationLabelProportionMeasurementPojo pojo = gson.fromJson(rawValue, ClassificationLabelProportionMeasurementPojo.class);
      int labelIndex = indexes.getLabelIndexer().indexOf(pojo.label);
      return new ClassificationLabelProportionMeasurement(annotator, labelIndex, pojo.value, pojo.confidence); 
    }
    
    else{
      throw new IllegalArgumentException("unknown measurement type: "+rawValue);
    }
    
  }
  
  
  public static class MeasurementPojos{

    public static class MeasurementPojo {
      private String type;
    }

    public static class ClassificationAnnotationMeasurementPojo extends MeasurementPojo{
      private String label;
      private String source;
      private double value;
      private Double confidence;
    }

    public static class ClassificationLabelProportionMeasurementPojo extends MeasurementPojo{
      private String label;
      private double value;
      private Double confidence;
    }

  }
  
}
