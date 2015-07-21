package edu.byu.nlp.data.types;

import edu.byu.nlp.util.Indexer;

public interface DatasetInstanceInfo {

	/**
	 * Equal to another DatsetInstance's source iff the 
	 * two objects represent the same instance. 
	 * 
	 * This is valid ONLY inside of single dataset
	 */
	int getSource();

  /**
   * Equal to another DatsetInstance's source iff the 
   * two objects represent the same instance. 
   * 
   * This can potentially be valid across different 
   * datasets, assuming that these strings (e.g., urls)
   * are unified across the datasets.
   */
	String getRawSource();

	/**
	 * NOT the same as Dataset.getNumAnnotators(). 
	 * Returns the number of annotators who have annotated 
	 * this DatasetInstance
	 */
	int getNumAnnotators();
	
	int getNumAnnotations();
	
	Indexer<String> getLabelIndexer();
	
	void annotationsChanged();
	
}
