package edu.byu.nlp.data;


public abstract class AbstractFlatInstance<D,L> implements FlatInstance<D, L> {
  
  @Override
  public String toString() {
          return getClass().getName()+" "+(isAnnotation()?"Annotation":"Label")+
                          "[src="+getSource()+", id="+getInstanceId()+", lab="+getLabel()+
                          ", ann="+getAnnotator()+", start="+getStartTimestamp()+", stop="+getEndTimestamp()+"]";
  }
  
  /**
   * Equality is determined entirely by instanceid
   */
  @Override
  public boolean equals(Object obj) {
          if (obj==null || !(obj instanceof FlatInstance<?,?>)){
                  return false;
          }
          return ((FlatInstance<?,?>)obj).getInstanceId() == getInstanceId();
  }
  
  /**
   * Identity (and consequently hashes) are determined entirely by instanceid
   */
  @Override
  public int hashCode() {
          return (int) getInstanceId();
  }

}
