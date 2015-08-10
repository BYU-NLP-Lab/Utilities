package edu.byu.nlp.data.types;

import java.util.Collection;

import org.apache.commons.math3.random.RandomGenerator;

public interface Dataset extends Iterable<DatasetInstance> {

	void shuffle(RandomGenerator rnd);
	
	DatasetInstance lookupInstance(String rawSource);
	
	Collection<Measurement> getMeasurements();
	
	DatasetInfo getInfo();
	
}
