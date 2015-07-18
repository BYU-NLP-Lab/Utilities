package edu.byu.nlp.data.streams;

import java.util.Iterator;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import edu.byu.nlp.io.AbstractIterable;
import edu.byu.nlp.util.Iterables2;

public class DataStream extends AbstractIterable<Map<String,Object>>{

  private Iterable<Map<String,Object>> data;
  private String name;

  public static DataStream withSource(String name, Iterable<Map<String,Object>> data){
    Preconditions.checkNotNull(data);
    DataStream stream = new DataStream();
    stream.data=data;
    stream.name=name;
    return stream;
  }

  /**
   * Transform a data stream by apply a function to an entire item/object 
   * at a time.
   */
  public DataStream transform(Function<Map<String,Object>, Map<String,Object>> transform){
    data = Iterables.transform(data, transform);
    return this;
  }

  /**
   * Split each item in the data stream into (potentially) multiple items
   * and return all resulting items in a single stream. 
   */
  public DataStream oneToMany(Function<Map<String,Object>, Iterable<Map<String,Object>>> oneToMany){
    data = Iterables2.flatten(Iterables.transform(data, oneToMany));
    return this;
  }

  /**
   * Eliminate all but the matching entries
   */
  public DataStream filter(Predicate<Map<String,Object>> filter){
    data = Iterables.filter(data, filter);
    return this;
  }
  
  @Override
  public Iterator<Map<String, Object>> iterator() {
    return data.iterator();
  }
  
  public String getName(){
    return name;
  }

}
