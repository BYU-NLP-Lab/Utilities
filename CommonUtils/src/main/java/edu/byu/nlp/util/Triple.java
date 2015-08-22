package edu.byu.nlp.util;

import java.io.Serializable;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public class Triple<F,S,T> implements Serializable{
  private static final long serialVersionUID = 1L;
  F first;
  S second;
  T third;

  protected Triple(){}
  
  public static<F,S,T> Triple<F,S,T> of(F first, S second, T third){
    Triple<F,S,T> triple = new Triple<F, S, T>();
    triple.first=first;
    triple.second=second;
    triple.third=third;
    return triple;
  }
  
  F getFirst(){
    return first;
  }
  S getSecond(){
    return second;
  }
  T getThird(){
    return third;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof Triple))
      return false;

    @SuppressWarnings("rawtypes")
    final Triple other = (Triple) o;

    return Objects.equal(this.getFirst(), other.getFirst())
        && Objects.equal(this.getSecond(), other.getSecond())
        && Objects.equal(this.getThird(), other.getThird());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getFirst(), getSecond(), getThird());
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(Triple.class)
      .add("first", getFirst())
      .add("second", getSecond())
      .add("third", getThird())
      .toString();
  }

}
