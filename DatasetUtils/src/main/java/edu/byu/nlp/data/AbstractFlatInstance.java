package edu.byu.nlp.data;

public abstract class AbstractFlatInstance<D,L> implements FlatInstance<D,L> {

	@Override
	public String toString() {
		return getClass().getName()+" "+(isAnnotation()?"Annotation":"Label")+
				"[src="+getSource()+", id="+getInstanceId()+", lab="+getLabel()+
				", ann="+getAnnotator()+", start="+getStartTimestamp()+", stop="+getEndTimestamp()+"]";
	}
	
	
	/**
	 * By default, sort by order of arrival (endtimestamp)
	 */
	@Override
	public int compareTo(FlatInstance<D, L> other) {
		// All fields used in this method should always exist. 
		// We want an exception if they don't, because 
		// something unexpected is happening. So no null checks.
		int order = Long.compare(getEndTimestamp(), other.getEndTimestamp());
		
	      if (order==0){
	        order = Long.compare(getStartTimestamp(), other.getStartTimestamp());
	      }
	      if (order==0){
	        order = Long.compare(getAnnotator(), other.getAnnotator());
	      }
	      if (order==0){
	        order = getSource().compareTo(other.getSource());
	      }
	      return order;
	}
	
}
