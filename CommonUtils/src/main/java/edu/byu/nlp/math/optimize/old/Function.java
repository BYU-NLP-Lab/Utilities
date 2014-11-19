package edu.byu.nlp.math.optimize.old;

/**
 * @author Dan Klein
 * @author rah67
 * @author gb07
 */
public interface Function<T> {
	
	
	/**
	 * Calculates the value of the function at the specified value.
	 * 
	 * @param x the value to evaluate at
	 * @return the value of the function for the specified value. 
	 */
	public double valueAt(T x);
}
