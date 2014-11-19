package edu.byu.nlp.data.types;

public interface DatasetInstanceInfo {

	String getSource();

	int getNumAnnotators();
	
	int getNumAnnotations();
}
