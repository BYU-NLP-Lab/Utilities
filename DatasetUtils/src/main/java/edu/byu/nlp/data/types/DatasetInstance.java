package edu.byu.nlp.data.types;



public interface DatasetInstance {

	/**
	 * Get the feature vector associated with this instance. 
	 */
	SparseFeatureVector asFeatureVector();
	
	/**
	 * Get information associated with this instance. 
	 */
	DatasetInstanceInfo getInfo();

	/**
	 * Does this instance have a known gold-standard label? 
	 */
	boolean hasObservedLabel();

	/**
	 * Does this instance have a gold-standard label (concealed or not)? 
	 */
	boolean hasLabel();

	/**
	 * Get the known gold-standard label. Returns null if none. 
	 */
	Integer getObservedLabel();
	
	/**
	 * Get a concealed gold-standard label. Training algorithms 
	 * are NOT allowed to do this. Only evaluation code should 
	 * access a concealed value.  
	 */
	Integer getLabel();
	
	/**
	 * Does this instance have a known gold-standard regressand? 
	 */
	boolean hasObservedRegressand();

	/**
	 * Get the known gold-standard regressand. Returns null if none. 
	 */
	Double getObservedRegressand();

	/**
	 * Get a concealed gold-standard label. Training algorithms 
	 * are NOT allowed to do this. Only evaluation code should 
	 * access a concealed value.  
	 */
	Double getRegressand();

	/**
	 * Does this instance have a concealed gold-standard regressand? 
	 */
	boolean hasRegressand();
	
	/**
	 * Get the human-generated imperfect annotations associated 
	 * with this instance.
	 */
	AnnotationSet getAnnotations();
	
	/**
	 * Does this instance have any annotations of any kind? 
	 */
	boolean hasAnnotations();

}
