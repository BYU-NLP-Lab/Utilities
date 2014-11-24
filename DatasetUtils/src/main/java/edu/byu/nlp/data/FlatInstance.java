package edu.byu.nlp.data;

/**
 * 
 * @author plf1
 *
 * This class acts as a facade providing a common interface for 
 * labeled instances and annotated instances. This allows pipes 
 * to be defined for only one rather than for both 
 * AutomaticAnnotation (label) and Annotation objects.
 *
 */
public interface FlatInstance<D,L> extends Comparable<FlatInstance<D,L>> {

	D getData();
	
	L getLabel();
	
	boolean isAnnotation();
	
	String getSource();
	
	long getStartTimestamp();
	
	long getEndTimestamp();
	
	long getAnnotator();
	
	long getInstanceId();
	
}
