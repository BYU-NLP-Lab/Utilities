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
	
	public static class MutableInteger{
		private Integer val = null;
		public static MutableInteger from(Integer val){
			MutableInteger mint = new MutableInteger();
			mint.setValue(val);
			return mint;
		}
		public void setValue(Integer val){
			this.val=val;
		}
		public Integer getValue(){
			return this.val;
		}
	}

}
