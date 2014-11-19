package edu.byu.nlp.math.optimize.old;

import java.util.LinkedList;
import java.util.Vector;

import edu.byu.nlp.util.DoubleArrays;

/**
 * @author Dan Klein
 */
public class LBFGSMinimizer implements GradientMinimizer {
  double EPS = 1e-10;
  int maxIterations = 20;
  int maxHistorySize = 5;
  LinkedList<double[]> inputDifferenceVectorList = new LinkedList<double[]>();
  LinkedList<double[]> derivativeDifferenceVectorList = new LinkedList<double[]>();

  private Vector<IterationListener> iterationListenerList = new Vector<IterationListener>();
  public void addIterationListener(IterationListener l) {
	  iterationListenerList.add(l);
  }
  
  public void removeIterationListener(IterationListener l) {
	  iterationListenerList.remove(l);
  }

  IterationEvent siEvent;
  protected void fireStartingEvent(double[] initialParameters, double initialValue, long startTime, long endTime) {
	  siEvent = null;
	  for(IterationListener l : iterationListenerList) {
		  if (siEvent == null) {
			  siEvent = new IterationEvent(this, 0, initialParameters, initialValue, startTime, endTime);
		  }
		  l.initialValues(siEvent);
	  }
  }

  EndOfIterationEvent eoiEvent;
  protected void fireEndOfIterationEvent(int iteration, double[] initialParameters, double initialValue, double[] endingParameters, double endingValue, long startTime, long endTime) {
	  eoiEvent = null;
	  for(IterationListener l : iterationListenerList) {
		  if (eoiEvent == null) {
			  eoiEvent = new EndOfIterationEvent(this, iteration, initialParameters, initialValue, endingParameters, endingValue, startTime, endTime);
		  }
		  l.endOfIteration(eoiEvent);
	  }
  }
  
  IterationEvent liEvent;
  protected void fireFinishedEvent(int iteration, double[] endingParameters, double endingValue, long startTime, long endTime) {
	  liEvent = null;
	  for(IterationListener l : iterationListenerList) {
		  if (liEvent == null) {
			  liEvent = new IterationEvent(this, iteration, endingParameters, endingValue, startTime, endTime);
		  }
		  l.endingValues(liEvent);
	  }
  }

  public double[] minimize(DifferentiableFunction function, double[] initial, double tolerance) {
	long startTime, iterationStartTime;
    startTime = System.currentTimeMillis();

    BacktrackingLineSearcher lineSearcher = new BacktrackingLineSearcher();
    lineSearcher.stepSizeMultiplier = 0.01;
    
    double[] guess = initial.clone();
    double value = function.valueAt(guess);
    double[] derivative = function.derivativeAt(guess);
    fireStartingEvent(guess, value, startTime, System.currentTimeMillis());

    for (int iteration = 0; iteration < maxIterations; iteration++) {
//      value = function.valueAt(guess);
      iterationStartTime = System.currentTimeMillis();
      double[] initialInverseHessianDiagonal = getInitialInverseHessianDiagonal(function);
      double[] direction = implicitMultiply(initialInverseHessianDiagonal, derivative);
//      System.out.println(" Derivative is: "+DoubleArrays2.toString(derivative, 100));
//      DoubleArrays2.assign(direction, derivative);
      DoubleArrays.multiplyToSelf(direction, -1.0);
//      System.out.println(" Looking in direction: "+DoubleArrays2.toString(direction, 100));
      double[] nextGuess = lineSearcher.minimize(function, guess, direction);
      double nextValue = function.valueAt(nextGuess);
      double[] nextDerivative = function.derivativeAt(nextGuess);
//      System.out.printf("Iteration %d ended with value %.6f\n",iteration + 1, nextValue);
      if (converged(value, nextValue, tolerance)) {
    	  long endTime = System.currentTimeMillis();
    	  fireEndOfIterationEvent(iteration + 1, guess, value, nextGuess, nextValue, iterationStartTime, endTime);
    	  fireFinishedEvent(iteration + 1, nextGuess, nextValue, startTime, endTime);
    	  return nextGuess;
      }
      updateHistories(guess, nextGuess, derivative,  nextDerivative);
      if (iteration == 0)
          lineSearcher.stepSizeMultiplier = 0.5;

      fireEndOfIterationEvent(iteration + 1, guess, value, nextGuess, nextValue, iterationStartTime, System.currentTimeMillis());
      guess = nextGuess;
      value = nextValue;
      derivative = nextDerivative;
    }
    //System.err.println("LBFGSMinimizer.minimize: Exceeded maxIterations without converging.");
	fireFinishedEvent(maxIterations, guess, value, startTime, System.currentTimeMillis());
    return guess;
  }

  private boolean converged(double value, double nextValue, double tolerance) {
    if (value == nextValue)
      return true;
    double valueChange = SloppyMath.abs(nextValue - value);
    double valueAverage = SloppyMath.abs(nextValue + value + EPS) / 2.0;
    if (valueChange / valueAverage < tolerance)
      return true;
    return false;
  }

  private void updateHistories(double[] guess, double[] nextGuess, double[] derivative, double[] nextDerivative) {
    double[] guessChange = DoubleArrays2.addMultiples(nextGuess, 1.0, guess, -1.0);
    double[] derivativeChange = DoubleArrays2.addMultiples(nextDerivative, 1.0, derivative,  -1.0);
    pushOntoList(guessChange, inputDifferenceVectorList);
    pushOntoList(derivativeChange,  derivativeDifferenceVectorList);
  }

  private void pushOntoList(double[] vector, LinkedList<double[]> vectorList) {
    vectorList.addFirst(vector);
    if (vectorList.size() > maxHistorySize)
      vectorList.removeLast();
  }

  private int historySize() {
    return inputDifferenceVectorList.size();
  }

  private double[] getInputDifference(int num) {
    // 0 is previous, 1 is the one before that
    return inputDifferenceVectorList.get(num);
  }

  private double[] getDerivativeDifference(int num) {
    return derivativeDifferenceVectorList.get(num);
  }

  private double[] getLastDerivativeDifference() {
    return derivativeDifferenceVectorList.getFirst();
  }

  private double[] getLastInputDifference() {
    return inputDifferenceVectorList.getFirst();
  }


  // TODO Warning! There is a restriction here; numFeatures * numClasses must be < historySize()
  // and no checking or (meaningful) exception is thrown
  private double[] implicitMultiply(double[] initialInverseHessianDiagonal, double[] derivative) {
    double[] rho = new double[initialInverseHessianDiagonal.length];
    double[] alpha = new double[initialInverseHessianDiagonal.length];
    double[] right = derivative.clone();
    // loop last backward
    for (int i = historySize()-1; i >= 0; i--) {
      double[] inputDifference = getInputDifference(i);
      double[] derivativeDifference = getDerivativeDifference(i);
      rho[i] = DoubleArrays2.innerProduct(inputDifference, derivativeDifference);
      if (rho[i] == 0.0)
        throw new RuntimeException("LBFGSMinimizer.implicitMultiply: Curvature problem.");
      alpha[i] = DoubleArrays2.innerProduct(inputDifference, right) / rho[i];
      right = DoubleArrays2.addMultiples(right, 1.0, derivativeDifference, -1.0*alpha[i]);
    }
    double[] left = DoubleArrays2.pointwiseMultiply(initialInverseHessianDiagonal, right);
    for (int i = 0; i < historySize(); i++) {
      double[] inputDifference = getInputDifference(i);
      double[] derivativeDifference = getDerivativeDifference(i);
      double beta = DoubleArrays2.innerProduct(derivativeDifference, left) / rho[i];
      left = DoubleArrays2.addMultiples(left, 1.0, inputDifference, alpha[i] - beta);
    }
    return left;
  }

  private double[] getInitialInverseHessianDiagonal(DifferentiableFunction function) {
    double scale = 1.0;
    if (derivativeDifferenceVectorList.size() >= 1) {
      double[] lastDerivativeDifference = getLastDerivativeDifference();
      double[] lastInputDifference = getLastInputDifference();
      double num = DoubleArrays2.innerProduct(lastDerivativeDifference, lastInputDifference);
      double den = DoubleArrays2.innerProduct(lastDerivativeDifference, lastDerivativeDifference);
      scale = num / den;
    }
    return DoubleArrays2.constantArray(scale, function.dimension());
  }

  public LBFGSMinimizer() {
  }

  public LBFGSMinimizer(int maxIterations) {
    this.maxIterations = maxIterations;
  }

  public LBFGSMinimizer(int maxIterations, Vector<IterationListener> iterationListenerList) {
	  this(maxIterations);
	  this.iterationListenerList = iterationListenerList;
  }

}
