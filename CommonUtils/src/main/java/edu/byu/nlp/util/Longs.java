package edu.byu.nlp.util;

import com.google.common.base.Preconditions;

public class Longs {

	/**
	 * Argument must be a an long encoded as a double. 
	 * Fails if the value is greater than the threshold from 
	 * being a long.
	 */
	public static long fromDouble(double d, double threshold){
		long rounded = (int)Math.round(d);
		Preconditions.checkArgument(Doubles.equals(d, rounded, threshold), 
				"Value "+d+" is outside of the threshold of being a long.");
		return rounded;
	}
	
}
