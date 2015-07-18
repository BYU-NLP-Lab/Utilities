package edu.byu.nlp.data.types;

import edu.byu.nlp.data.streams.IndexerCalculator;
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
	
	int getNumAnnotations();
	
	int getNumFeatures();
	
	int getNumClasses();
	
	Indexer<String> getAnnotatorIdIndexer();
	
	Indexer<String> getInstanceIdIndexer();
	
	Indexer<String> getLabelIndexer();
	
	Indexer<String> getFeatureIndexer();
	
	IndexerCalculator<String,String> getIndexers();
	
	int getNullLabel();
	
	int getNumAnnotators();

	void annotationsChanged();
	
}
