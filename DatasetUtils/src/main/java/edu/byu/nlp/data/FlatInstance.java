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
public interface FlatInstance<D,L> {

	public static long NULL_TIMESTAMP = -1;
	public static long NULL_ID = -1;
	
	/**
	 * The data associated with this instance
	 * (often a String or featurevector) 
	 */
	D getData();
	
	/**
	 * For an annotated instance, the value of the annotation 
	 * provided. For a labeled instance, the value of the 
	 * automatic label. 
	 */
	L getLabel();
	
	/**
	 * Returns true if this instance represents an annotated 
	 * instance; otherwise false.
	 */
	boolean isAnnotation();
	
	/**
	 * The moment at which the instance was first presented 
	 * to the annotator.
	 */
	long getStartTimestamp();
	
	/**
	 * The moment at which the instance annotation was finished. 
	 */
	long getEndTimestamp();
	
	/**
	 * The id of the annotator (automatic or otherwise) that 
	 * produced either the annotation or automatic value. 
	 * For accepted gold-standard labels, this value should 
	 * be equal to Constants.GOLD_AUTOMATIC_ANNOTATOR_ID
	 */
	long getAnnotator();
	
	/**
	 * Returns a number that uniquely identifies the 
	 * instance annotated or labeled here.
	 */
	long getInstanceId();
	
	/**
	 * Returns a string representing the origin of this 
	 * instance such as a url or file path. 
	 * Should be a human-readable stand-in for
	 * instanceId. 
	 */
	String getSource();

	/**
	 * For an annotation instance, this is the value of the 
	 * automatic assistance label provided to 
	 * the annotator by way of a pre-annotation.
	 * 
	 * For a labeled instance, this value is undefined 
	 * and should be ignored.
	 */
	L getAutomaticLabel();
	
	/**
	 * If an automatic annotation exists, returns the id of the 
	 * model used to produce that label
	 */
	long getAutomaticLabelerId();
	
}
