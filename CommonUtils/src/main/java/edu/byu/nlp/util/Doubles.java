package edu.byu.nlp.util;


public class Doubles {

	private Doubles(){}
	
	public static boolean equals(double d1, double d2, double threshold){
		return Math.abs(d1-d2) < threshold;
	}
	
}
