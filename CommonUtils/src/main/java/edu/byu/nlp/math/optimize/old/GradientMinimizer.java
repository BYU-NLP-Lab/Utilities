package edu.byu.nlp.math.optimize.old;

/**
 * @author Dan Klein
 */
public interface GradientMinimizer {
  double[] minimize(DifferentiableFunction function, double[] initial, double tolerance);
}
