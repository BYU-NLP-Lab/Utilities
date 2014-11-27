package edu.byu.nlp.util;

import com.google.common.base.Preconditions;

public class Integers {

	/**
	 * Assumes that the double encodes and int within 
	 * the specified threshold. Fails 
	 * if the value is outside of the threshold of being 
	 * an int or if the value is too large or small to be 
	 * represented as an int.
	 */
	public static int fromDouble(double value, double threshold){
		long lng = Longs.fromDouble(value, threshold);
		// make sure it's not so big that casting to int loses information
		Preconditions.checkState((int)lng == lng); 
		return (int)lng;
	}

}
