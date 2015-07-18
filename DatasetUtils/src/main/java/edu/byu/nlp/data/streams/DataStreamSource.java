package edu.byu.nlp.data.streams;

import java.util.Map;

public interface DataStreamSource {

  Iterable<Map<String,Object>> getStream();
  String getStreamSource();
  
}
