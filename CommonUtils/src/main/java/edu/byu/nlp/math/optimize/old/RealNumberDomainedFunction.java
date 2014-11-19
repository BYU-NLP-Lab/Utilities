package edu.byu.nlp.math.optimize.old;

/**
 * @author Dan Klein
 * @author rah67
 */
public interface RealNumberDomainedFunction extends Function<double[]> {
	
	/**
	 * @return The dimension of the function
	 */
	public int dimension();

	/**
	 * Calculates the value of the function at the specified values of the ind. vars.
	 * 
	 * @param x the values of the independent variables
	 * @return the value of the function for the specified values of the ind. vars. 
	 */
	@Override
	public double valueAt(double... x);
}
