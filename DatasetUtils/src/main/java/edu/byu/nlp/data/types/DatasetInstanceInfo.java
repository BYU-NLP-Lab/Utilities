package edu.byu.nlp.data.types;

import edu.byu.nlp.util.Indexer;

public interface DatasetInstanceInfo {

	/**
	 * Equal to another DatsetInstance's id iff the 
	 * two objects represent the same instance. 
	 */
	long getInstanceId();

	/**
	 * Equal to another DatsetInstance's source iff the 
	 * two objects represent the same instance. 
	 * This is technically redundant with instance 
	 * id but is more human readable.
	 */
	String getSource();

	int getNumAnnotators();
	
	int getNumAnnotations();
	
	Indexer<String> getLabelIndexer();
	
	void updateAnnotationInfo();
}