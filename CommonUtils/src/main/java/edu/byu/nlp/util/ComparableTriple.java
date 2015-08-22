package edu.byu.nlp.util;

import com.google.common.collect.ComparisonChain;

public class ComparableTriple<F extends Comparable<F>, S extends Comparable<S>, T extends Comparable<T>> extends Triple<F,S,T> implements Comparable<Triple<F,S,T>>{
  private static final long serialVersionUID = 1L;

  protected ComparableTriple(){
    super();
  }
  
  public static<F extends Comparable<F>, S extends Comparable<S>, T extends Comparable<T>> Triple<F,S,T> of(F first, S second, T third){
    ComparableTriple<F,S,T> triple = new ComparableTriple<F, S, T>();
    triple.first=first;
    triple.second=second;
    triple.third=third;
    return triple;
  }
  
  @Override
  public int compareTo(Triple<F, S, T> o) {
    return ComparisonChain.start()
        .compare(getFirst(), o.getFirst())
        .compare(getSecond(), o.getSecond())
        .compare(getThird(), o.getThird())
        .result();
  }

}
