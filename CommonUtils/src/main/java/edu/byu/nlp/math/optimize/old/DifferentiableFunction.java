package edu.byu.nlp.math.optimize.old;


/**
 * @author Dan Klein
 * @author rah67
 */
public interface DifferentiableFunction extends RealNumberDomainedFunction {
  double[] derivativeAt(double... x);
}
