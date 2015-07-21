package edu.byu.nlp.data.pipes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import edu.byu.nlp.data.streams.DataStream;
import edu.byu.nlp.data.streams.DataStreams;
import edu.byu.nlp.util.Maps2;

public class DataStreamsTest {

  
  private Iterable<Map<String,Object>> simpleStream(){
    List<Map<String,Object>> stream = Lists.newArrayList();
    stream.add(Maps2.hashmapOf(new String[]{"source","annotator","annotation"}, new Object[]{"item1","john",1}));
    stream.add(Maps2.hashmapOf(new String[]{"source","annotator","annotation"}, new Object[]{"item2","john",2}));
    stream.add(Maps2.hashmapOf(new String[]{"source","annotator","annotation"}, new Object[]{"item1","sally",1}));
    stream.add(Maps2.hashmapOf(new String[]{"source","annotator","annotation"}, new Object[]{"item2","sally",3}));
    return stream;
  }

  @Test
  public void testLazyEval() {
    Iterable<Map<String, Object>> stream = simpleStream();
    Iterable<Map<String, Object>> xstream = DataStream.withSource("teststream", stream) 
        .transform(DataStreams.Transforms.transformFieldValue("source", new Function<String, String>() {
      @Override
      public String apply(String input) {
        return input.replace("item", "instance");
      }
    }));
    // ensure nothing has changed yet in the original stream (should be done lazily)
    ArrayList<Map<String, Object>> items = Lists.newArrayList(stream);
    Assertions.assertThat(items.get(0).get("source")).isEqualTo("item1");
    Assertions.assertThat(items.get(1).get("source")).isEqualTo("item2");
    Assertions.assertThat(items.get(2).get("source")).isEqualTo("item1");
    // now run the pipe and check for changes
    ArrayList<Map<String, Object>> xitems = Lists.newArrayList(xstream);
    Assertions.assertThat(xitems.get(0).get("source")).isEqualTo("instance1");
    Assertions.assertThat(xitems.get(1).get("source")).isEqualTo("instance2");
    Assertions.assertThat(xitems.get(2).get("source")).isEqualTo("instance1");
  }

  @Test
  public void testFieldRename() {
    Iterable<Map<String, Object>> stream = DataStream.withSource("testdat", simpleStream())
        .transform(DataStreams.Transforms.renameField("source", "wherefrom"));
    // now run the pipe and check for changes
    for (Map<String, Object> item: stream){
      Assertions.assertThat(item.containsKey("source")).isFalse();
      Assertions.assertThat(item.containsKey("wherefrom")).isTrue();
    }
  }

  @Test
  public void testFilter() {
    Iterable<Map<String, Object>> stream = DataStream.withSource("testdata", simpleStream())
        .filter(DataStreams.Filters.filterByFieldValue("annotation", new Predicate<Integer>() {
      @Override
      public boolean apply(Integer input) {
        return input>1;
      }
    }));
    // now run the pipe and check for changes
    ArrayList<Map<String, Object>> items = Lists.newArrayList(stream);
    Assertions.assertThat(items.size()).isEqualTo(2);
    Assertions.assertThat(items.get(0).get("annotator")).isEqualTo("john");
    Assertions.assertThat(items.get(0).get("annotation")).isEqualTo(2);
    Assertions.assertThat(items.get(1).get("annotator")).isEqualTo("sally");
    Assertions.assertThat(items.get(1).get("annotation")).isEqualTo(3);
  }

  @Test
  public void testOneToMany() {
    Iterable<Map<String, Object>> stream = DataStream.withSource("testdata", simpleStream())
        .oneToMany(DataStreams.OneToManys.duplicate(5));
    // now run the pipe and check for changes
    ArrayList<Map<String, Object>> items = Lists.newArrayList(stream);
    Assertions.assertThat(items.size()).isEqualTo(4*5);
    for (int i=0; i<5; i++){
      Assertions.assertThat(items.get(i).get("source")).isEqualTo("item1");
      Assertions.assertThat(items.get(i).get("annotator")).isEqualTo("john");
      Assertions.assertThat(items.get(i).get("annotation")).isEqualTo(1);
    }
    for (int i=6; i<10; i++){
      Assertions.assertThat(items.get(i).get("source")).isEqualTo("item2");
      Assertions.assertThat(items.get(i).get("annotator")).isEqualTo("john");
      Assertions.assertThat(items.get(i).get("annotation")).isEqualTo(2);
    }
    for (int i=15; i<19; i++){
      Assertions.assertThat(items.get(i).get("source")).isEqualTo("item2");
      Assertions.assertThat(items.get(i).get("annotator")).isEqualTo("sally");
      Assertions.assertThat(items.get(i).get("annotation")).isEqualTo(3);
    }
  }
  
}
