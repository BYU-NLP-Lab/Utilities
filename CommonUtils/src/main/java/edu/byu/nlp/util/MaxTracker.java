package edu.byu.nlp.util;

public class MaxTracker {

	private double maxDbl = Double.NEGATIVE_INFINITY;
	private long maxLng = Long.MIN_VALUE;
	
	public void offerDouble(double val){
		if (val>maxDbl){
			maxDbl = val;
		}
	}
	
	public void offerLong(long val){
		if (val>maxLng){
			maxLng = val;
		}
	}
	
	public double maxDouble(){
		return maxDbl;
	}

	public long maxLong(){
		return maxLng;
	}
	
	public double max(){
		return Math.max(maxLng, maxDbl);
	}
}
