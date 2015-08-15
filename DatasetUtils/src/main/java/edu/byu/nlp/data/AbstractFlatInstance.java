package edu.byu.nlp.data;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ComparisonChain;


public abstract class AbstractFlatInstance<D,L> implements FlatInstance<D, L> {
  
  @Override
  public String toString() {
    return MoreObjects.toStringHelper(AbstractFlatInstance.class)
        .add("type", isAnnotation()? "annotation": isMeasurement()? "measurement": "label")
        .toString();
//          return getClass().getName()+" "+(isAnnotation()?"Annotation":"Label")+
//                          "[src="+getSource()+", id="+getInstanceId()+", lab="+getLabel()+
//                          ", ann="+getAnnotation()+", annid="+getAnnotator()+", start="+getStartTimestamp()+", stop="+getEndTimestamp()+"]";
  }
  
  // equality is all object equality
  @Override
  public final boolean equals(Object obj) {
    return super.equals(obj);
  }
//  /**
//   * Equality is determined entirely by source (so it work inter-dataset)
//   */
//  @Override
//  public boolean equals(Object obj) {
//    // for annotations and measurements, use object equality (all are unique)
//    if (isAnnotation() || isMeasurement()){
//      return super.equals(obj);
//    }
//    // for instances, use source 
//    if (obj==null || !(obj instanceof FlatInstance<?,?>)){
//            return false;
//    }
//    FlatInstance<?, ?> other = ((FlatInstance<?,?>)obj);
//    return Objects.equal(other.getSource(), getSource());
//  }

  @Override
  public final int hashCode() {
    return super.hashCode();
  }
//  /**
//   * Identity (and consequently hashes) are determined entirely by instanceid
//   */
//  @Override
//  public int hashCode() {
//          return (int) getInstanceId();
//  }
  
  @Override
  public int compareTo(FlatInstance<D, L> o) {
    return ComparisonChain.start()
        .compare(getSource(), o.getSource())
        .compare(getInstanceId(), o.getInstanceId())
        .compare(getAnnotator(), o.getAnnotator())
        .compare(getEndTimestamp(), o.getEndTimestamp())
        .compare(getStartTimestamp(), o.getStartTimestamp())
        .compare(getMeasurement(), o.getMeasurement())
        .result();
  }
  

}
