package edu.byu.nlp.data.streams;

import java.util.Map;

public interface DataStreamSink<O> {

  public O process(Iterable<Map<String, Object>> stream);

}