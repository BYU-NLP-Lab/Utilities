package edu.byu.nlp.math.optimize.old;

/**
 * @author Dan Klein
 */
public interface GradientLineSearcher {
  public double[] minimize(DifferentiableFunction function, double[] initial, double[] direction);
}
