package edu.byu.nlp.data.streams;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.byu.nlp.util.Iterables2;

/**
 * Data streams are are represented as Iterable<Map<String,Object>> 
 * where objects are the (fieldname, value) pairs in the maps.
 * 
 * The idea is that we import data in a pipe like this, 
 * manipulate it as much as we want, then import it into 
 * a statically-defined data structure (i.e., Dataset).
 *  
 * Although this map representation doesn't allow type checking, 
 * the advantages of flexibility and simplicity outweigh the downsides. 
 * It permits easy import of lists of json objects 
 * and general manipulation without resorting to reflection, 
 * including operations like changing adding, removing, or 
 * altering field names. 
 * It also allows us to use Guava's Function and Iterable 
 * methods instead of rolling our own. 
 * 
 * For example, Iterables.transform(stream1,function) 
 */
public class DataStreams {

  /* ******************************************************************************************** */
  /* **************************** One-to-many *************************************************** */
  /* ******************************************************************************************** */
  public static interface OneToMany extends Function<Map<String,Object>,Iterable<Map<String,Object>>>{};

  public static class OneToManys{
    public static OneToMany duplicate(final int n){
      return new OneToMany() {
        @SuppressWarnings("unchecked")
        @Override
        public Iterable<Map<String, Object>> apply(Map<String, Object> input) {
          return Iterables.limit(Iterables.cycle(input), n); 
        }
      };
    }
    /**
     * Use one-to-many function that operates on an attribute value. Replicate the whole item 
     * once for each resulting value. 
     */
    @SuppressWarnings("rawtypes")
    public static OneToMany oneToManyByFieldValue(final String field, final Function fieldSplitterFunction){
      return new OneToMany(){
        @SuppressWarnings("unchecked")
        @Override
        public Iterable<Map<String, Object>> apply(final Map<String, Object> input) {
          if (input.containsKey(field)){
            Iterable<Object> values = (Iterable<Object>) fieldSplitterFunction.apply(input.get(field));
            // now create a copy of it input for each value (use guava's transform function
            // since it evaluates lazily
            return Iterables.transform(values, new Function<Object,Map<String,Object>>(){
              @Override
              public Map<String, Object> apply(Object value) {
                HashMap<String, Object> clone = Maps.newHashMap(input);
                clone.put(field, value);
                return clone;
              }
            });
          }
          return Lists.newArrayList();
        }
      };
    }
  }

  /* ******************************************************************************************** */
  /* ***************************** Filter ******************************************************* */
  /* ******************************************************************************************** */
  public static interface Filter extends Predicate<Map<String,Object>>{};
  
  public static class Filters{
    @SuppressWarnings("rawtypes")
    public static Filter filterByFieldValue(final String field, final Predicate fieldPredicate){
      return new Filter() {
        @SuppressWarnings("unchecked")
        @Override
        public boolean apply(Map<String, Object> input) {
          if (input.containsKey(field)){
            return fieldPredicate.apply(input.get(field));
          }
          return false;
        }
      };
    }
  }
  

  /* ******************************************************************************************** */
  /* ****************************** Transform *************************************************** */
  /* ******************************************************************************************** */
  public static interface Transform extends Function<Map<String,Object>, Map<String,Object>>{};

  public static class Transforms{

    /**
     * Transform a particular field value. 
     * Casts transforms without checking type compatility, so 
     * errors won't show up until run time.
     */
    @SuppressWarnings("rawtypes")
    public static Transform transformFieldValue(final String field, final Function fieldTransform){
      return new Transform(){
        @SuppressWarnings("unchecked")
        @Override
        public Map<String, Object> apply(Map<String, Object> input) {
          if (input.containsKey(field)){
            input.put(field, fieldTransform.apply(input.get(field)));
          }
          return input;
        }
      };
    }

    /**
     * Transform a data stream by changing the name of a field 
     */
    public static Transform renameField(final String fromField, final String toField){
      return new Transform(){
        @Override
        public Map<String, Object> apply(Map<String, Object> input) {
          if (input.containsKey(fromField)){
            // add new field
            input.put(toField, input.get(fromField));
            // remove old field
            input.remove(fromField);
          }
          return input;
        }
      };
    }
    
  }
  

}
