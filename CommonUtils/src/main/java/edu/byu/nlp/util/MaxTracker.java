package edu.byu.nlp.util;

public class MaxTracker {

	private double maxDbl = Double.NEGATIVE_INFINITY;
	private long maxLng = Long.MIN_VALUE;

	public void offerIntsAsLongs(Iterable<Integer> vals){
		for (Integer val: vals){
			offerLong(val);
		}
	}
	
	public void offerLongs(Iterable<Long> vals){
		for (Long val: vals){
			offerLong(val);
		}
	}
	
	public void offerDoubles(Iterable<Double> vals){
		for (Double val: vals){
			offerDouble(val);
		}
	}
	
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
