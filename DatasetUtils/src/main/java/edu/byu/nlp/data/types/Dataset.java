package edu.byu.nlp.data.types;

import org.apache.commons.math3.random.RandomGenerator;

public interface Dataset extends Iterable<DatasetInstance> {

	void shuffle(RandomGenerator rnd);
	
	DatasetInfo getInfo();
	
	DatasetInstance lookupInstance(String source);
	
}
