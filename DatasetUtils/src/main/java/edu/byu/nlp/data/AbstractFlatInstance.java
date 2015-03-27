package edu.byu.nlp.data;

public abstract class AbstractFlatInstance<D,L> implements FlatInstance<D,L> {

	@Override
	public String toString() {
		return getClass().getName()+" "+(isAnnotation()?"Annotation":"Label")+
				"[src="+getSource()+", id="+getInstanceId()+", lab="+getLabel()+
				", ann="+getAnnotator()+", start="+getStartTimestamp()+", stop="+getEndTimestamp()+"]";
	}
	
}
