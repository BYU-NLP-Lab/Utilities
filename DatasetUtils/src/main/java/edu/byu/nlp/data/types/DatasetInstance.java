package edu.byu.nlp.data.types;



public interface DatasetInstance {

	/**
	 * Get the features associated with this instance 
	 * as a single vector. This representation is 
	 * usually suitable for classification or clustering.
	 */
	SparseFeatureVector asFeatureVector();
	
	/**
	 * Get the features associated with this instance 
	 * as a matrix. This representation is usually 
	 * suitable for modeling sequences (tagging) 
	 * or some other kind of structured prediction (trees). 
	 */
	SparseFeatureMatrix asFeatureMatrix();
	
	/**
	 * Get information associated with this instance. 
	 */
	DatasetInstanceInfo getInfo();

	/**
	 * Does this instance have a known gold-standard label? 
	 */
	boolean hasLabel();

	/**
	 * Get the known gold-standard label. Returns null if none. 
	 */
	Integer getLabel();
	
	/**
	 * Conceal the gold-standard label. For the purposes 
	 * of training algorithms, this removes the label. 
	 */
	void setLabelConcealed(boolean concealed);
	
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
	 * Conceal the gold-standard regressand. For the purposes 
	 * of training algorithms, this removes the label. 
	 */
	void setRegressandConcealed(boolean concealed);

	/**
	 * Get a concealed gold-standard label. Training algorithms 
	 * are NOT allowed to do this. Only evaluation code should 
	 * access a concealed value.  
	 */
	Double getConcealedRegressand();
	
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
