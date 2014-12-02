package edu.byu.nlp.data.types;

import edu.byu.nlp.util.Indexer;

public interface DatasetInfo {

	String getSource();
	
	int getNumDocuments();

	int getNumDocumentsWithAnnotations();

	int getNumDocumentsWithoutAnnotations();

	int getNumDocumentsWithLabels();

	int getNumDocumentsWithoutLabels();
	
	int getNumDocumentsWithObservedLabels();

	int getNumDocumentsWithoutObservedLabels();

	int getNumTokens();

	int getNumTokensWithAnnotations();

	int getNumTokensWithoutAnnotations();
	
	int getNumTokensWithObservedLabels();

	int getNumTokensWithoutObservedLabels();
	
	int getNumTokensWithLabels();
	
	int getNumTokensWithoutLabels();
	
	int getNumFeatures();
	
	int getNumClasses();
	
	Indexer<Long> getAnnotatorIdIndexer();
	
	Indexer<Long> getInstanceIdIndexer();
	
	Indexer<String> getLabelIndexer();
	
	Indexer<String> getFeatureIndexer();
	
	int getNullLabel();
	
	int getNumAnnotators();
	
}
