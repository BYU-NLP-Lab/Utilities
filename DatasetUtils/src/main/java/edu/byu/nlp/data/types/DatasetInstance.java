package edu.byu.nlp.data.types;



public interface DatasetInstance {

	/**
	 * Get the features associated with this instance 
	 * as a single vector. This representation is 
	 * usually suitable for classification or clustering.
	 */
	SparseFeatureVector asFeatureVector();
	
	/**
	 * Get information associated with this instance. 
	 */
	DatasetInstanceInfo getInfo();

	/**
	 * Does this instance have a known gold-standard label? 
	 */
	boolean hasLabel();

	/**
	 * Does this instance have a concealed gold-standard label? 
	 */
	boolean hasConcealedLabel();

	/**
	 * Get the known gold-standard label. Returns null if none. 
	 */
	Integer getLabel();
	
	/**
	 * Get a concealed gold-standard label. Training algorithms 
	 * are NOT allowed to do this. Only evaluation code should 
	 * access a concealed value.  
	 */
	Integer getConcealedLabel();
	
	/**
	 * Does this instance have a known gold-standard regressand? 
	 */
	boolean hasRegressand();

	/**
	 * Get the known gold-standard regressand. Returns null if none. 
	 */
	Double getRegressand();

	/**
	 * Get a concealed gold-standard label. Training algorithms 
	 * are NOT allowed to do this. Only evaluation code should 
	 * access a concealed value.  
	 */
	Double getConcealedRegressand();

	/**
	 * Does this instance have a concealed gold-standard regressand? 
	 */
	boolean hasConcealedRegressand();
	
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
