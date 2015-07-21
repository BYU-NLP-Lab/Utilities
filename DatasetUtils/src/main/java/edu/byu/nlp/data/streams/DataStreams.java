package edu.byu.nlp.data.streams;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  private static final Logger logger = LoggerFactory.getLogger(DataStreams.class);

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
     * once for each resulting value. Items that do not have in indicated field are 
     * ommitted from the resulting stream.
     */
    @SuppressWarnings("rawtypes")
    public static OneToMany oneToManyByFieldValue(final String field, final Function fieldSplitterFunction){
      return new OneToMany(){
        @SuppressWarnings("unchecked")
        @Override
        public Iterable<Map<String, Object>> apply(final Map<String, Object> input) {
          if (input.containsKey(field)){
            Iterable<Object> values = (Iterable<Object>) fieldSplitterFunction.apply(input.get(field));
            // now create a copy of the input for each value (use guava's transform function
            // since it evaluates lazily
            return Iterables.transform(values, new Function<Object,Map<String,Object>>(){
              @Override
              public Map<String, Object> apply(Object value) {
                HashMap<String, Object> clone = Maps.newHashMap(input); // clone to avoid side effects
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

    public static Transform passThrough(){
      return new Transform() {
        @Override
        public Map<String, Object> apply(Map<String, Object> input) {
          return input;
        }
      };
    }
    
    /**
     * Transform a particular field value. 
     * Casts transforms without checking type compatibility, so 
     * errors won't show up until run time.
     */
    public static <I,O> Transform transformFieldValue(final String field, final Function<I,O> fieldTransform){
      return transformFieldValue(field, field, fieldTransform);
    }
    
    public static <I,O> Transform transformFieldValue(final String field, final String targetField, final Function<I,O> fieldTransform){
      if (field==null || fieldTransform==null){
        logger.warn("A null value was passed to transformFieldValue(). Returning null.");
        return null;
      }
      return new Transform(){
        @SuppressWarnings("unchecked")
        @Override
        public Map<String, Object> apply(Map<String, Object> input) {
          if (input.containsKey(field)){
            // this is where the critical cast happens 
            // if types are messed up in a pipeline, you won't get any compile errors.
            // instead, you will get a ClassCastException here.
            try{
              input = Maps.newHashMap(input); // clone to avoid side-effects
              
              I inval = (I) input.get(field);
              O outval = fieldTransform.apply(inval); 
              input.put(targetField, outval);
            }
            catch (ClassCastException e){
              throw new IllegalArgumentException("Detected a pipeline with invalid types. The transform "
                  +fieldTransform.getClass().getName()+" couldn't cast "+input.get(field)+" to its desired input", e);
            }
          }
          return input;
        }
      };
    }
    
    /**
     * Same as transformFieldValue(), but assume that the field value is an iterable, 
     * and the provided transform function is to be applied to each item in that iterable.
     * 
     * Items that are changed to null are removed form the resulting iterable.
     * 
     * input/output types should be the type of the items of the iterable.
     */
    public static <I,O> Function<Map<String, Object>, Map<String, Object>> transformIterableFieldValues(final String field, final Function<I,O> itemTransform) {
      if (field==null || itemTransform==null){
        logger.warn("A null value was passed to transformIterableFieldValues(). Returning null.");
        return null;
      }
      return transformFieldValue(field, new Function<Iterable<I>,Iterable<O>>() {
        @Override
        public Iterable<O> apply(Iterable<I> inval) {
          return 
              Iterables2.filterNullValues( // remove null values
              Iterables.transform(inval, itemTransform) // do the transform
              );
        }
      });
    }

    /**
     * Same as transformIterableFieldValues(), but assume that the field value is an iterable of iterables, 
     * and the provided transform function is to be applied to each item in that collection.
     * 
     * Items that are changed to null are removed form the resulting iterable.
     * 
     * input/output types should be the type of the items of the innermost iterable.
     */
    public static <I,O> Function<Map<String, Object>, Map<String, Object>> transformIterableIterableFieldValues(final String field, final Function<I,O> itemTransform) {
      if (field==null || itemTransform==null){
        logger.warn("A null value was passed to transformIterableIterableFieldValues(). Returning null.");
        return null;
      }
      return transformFieldValue(field, new Function<Iterable<Iterable<I>>,Iterable<Iterable<O>>>() {
        @Override
        public Iterable<Iterable<O>> apply(Iterable<Iterable<I>> inval) {
          return Iterables2.filterNullValuesFromIterables( // remove null values
              Iterables2.transformIterables(inval, itemTransform) // do the transform
              ); 
        }
      });
    }

    /**
     * Transform a data stream by changing the name of a field 
     */
    public static Transform renameField(final String fromField, final String toField){
      return new Transform(){
        @Override
        public Map<String, Object> apply(Map<String, Object> input) {
          if (input.containsKey(fromField)){
            input = Maps.newHashMap(input); // clone to avoid side-effects
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
