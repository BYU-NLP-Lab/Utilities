package edu.byu.nlp.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class Maps2 {

  private Maps2(){}

  public static <K,V> Map<K,V> hashmapOf(List<K> keys, List<V> vals){
    Preconditions.checkNotNull(keys);
    Preconditions.checkNotNull(vals);
    Preconditions.checkArgument(keys.size()==vals.size());
    HashMap<K, V> map = Maps.newHashMap();
    for (int i=0; i<keys.size(); i++){
      map.put(keys.get(i), vals.get(i));
    }
    return map;
  }
  
  public static <K,V> Map<K,V> hashmapOf(K[] keys, V[] vals){
    Preconditions.checkNotNull(keys);
    Preconditions.checkNotNull(vals);
    Preconditions.checkArgument(keys.length==vals.length);
    HashMap<K, V> map = Maps.newHashMap();
    for (int i=0; i<keys.length; i++){
      map.put(keys[i], vals[i]);
    }
    return map;
  }
  
  
}
