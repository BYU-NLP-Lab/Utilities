package edu.byu.nlp.data;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;


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
  
  /**
   * Equality is determined entirely by source (so it work inter-dataset)
   */
  @Override
  public boolean equals(Object obj) {
    // for annotations and measurements, use object equality (all are unique)
    if (isAnnotation() || isMeasurement()){
      return super.equals(obj);
    }
    // for instances, use source 
    if (obj==null || !(obj instanceof FlatInstance<?,?>)){
            return false;
    }
    FlatInstance<?, ?> other = ((FlatInstance<?,?>)obj);
    return Objects.equal(other.getSource(), getSource());
  }
  
  /**
   * Identity (and consequently hashes) are determined entirely by instanceid
   */
  @Override
  public int hashCode() {
          return (int) getInstanceId();
  }

}
