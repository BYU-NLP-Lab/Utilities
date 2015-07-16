package edu.byu.nlp.data.pipes;

import java.util.Map;

public interface DataSource {

  Iterable<Map<String,Object>> getStream();
  String getStreamSource();
  
}
