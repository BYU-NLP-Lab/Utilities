package edu.byu.nlp.data.types;

import edu.byu.nlp.util.Indexer;

public interface DatasetInfo {

	String getSource();
	
	int getNumDocuments();
	
	int getNumTokens();
	
	int getNumFeatures();
	
	int getNumClasses();
	
	Indexer<Long> getAnnotatorIdIndexer();
	
	Indexer<Long> getInstanceIdIndexer();
	
	Indexer<String> getLabelIndexer();
	
	Indexer<String> getFeatureIndexer();
	
}
