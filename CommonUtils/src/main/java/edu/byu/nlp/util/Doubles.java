package edu.byu.nlp.util;

import com.google.common.base.Preconditions;

public class Doubles {

	private Doubles(){}
	
	public static boolean equals(double d1, double d2, double threshold){
		return Math.abs(d1-d2) < threshold;
	}
	
	public static boolean isLong(double d, double threshold){
		return equals(d, Math.round(d), threshold);
	}
	
	/**
	 * Argument must be a an int/long encoded as a double
	 */
	public static long longFrom(double d, double threshold){
		long rounded = (int)Math.round(d);
		Preconditions.checkArgument(equals(d, rounded, threshold));
		return rounded;
	}
	
}
