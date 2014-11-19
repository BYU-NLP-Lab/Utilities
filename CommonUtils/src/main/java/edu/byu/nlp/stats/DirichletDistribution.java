/**
 * Copyright 2012 Brigham Young University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.byu.nlp.stats;

import java.util.Arrays;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.random.RandomGenerator;

import com.google.common.base.Preconditions;

import edu.byu.nlp.math.GammaFunctions;
import edu.byu.nlp.math.RealMatrices;
import edu.byu.nlp.util.DoubleArrays;

/**
 * @author rah67
 *
 */
public class DirichletDistribution {

	private final double[] alpha;
	
	public static DirichletDistribution newDirichlet(double[] alpha, boolean copy) {
		if (copy) {
			alpha = alpha.clone();
		}
		return new DirichletDistribution(alpha);
	}
	
	private DirichletDistribution(double[] alpha) {
		this.alpha = alpha;
	}
	
	public double[] sample(RandomGenerator rnd) {
		return sample(alpha, rnd);
	}

	/** alpha is output parameter **/
	public static void methodOfMoments(double[][] data, double[] alpha) {
		Preconditions.checkNotNull(data);
		Preconditions.checkNotNull(alpha);

		if (data.length == 1) {
			System.arraycopy(data[0], 0, alpha, 0, alpha.length);
			DoubleArrays.expToSelf(alpha);
			return;
		}
	
		double[][] moments = moments(data, alpha.length);
		double[] expectedX = moments[0];
		double[] expectedXSquared = moments[1];
		
		// TODO : optimize
		for (int k = 0; k < expectedX.length; k++) {
			alpha[k] = expectedX[k] * (expectedX[0] - expectedXSquared[0]) / 
					(expectedXSquared[0] - expectedX[0] * expectedX[0]); 
		}
	}
	
	// Index 0 of returned value is E[x]; Index 1 is E[x^2] (non-central) 
	private static double[][] moments(double[][] data, int K) {
		double[] expectedX = new double[K];
		double[] expectedXSquared = new double[K];
		
		for (double[] theta : data) {
			if (theta.length != K) {
				throw new IllegalArgumentException("Dimensions of data and alpha do not match!");
			}
			for (int k = 0; k < expectedX.length; k++) {
				double p = Math.exp(theta[k]);
				expectedX[k] += p;
				expectedXSquared[k] += p * p;
			}
		}
		DoubleArrays.divideToSelf(expectedX, data.length);
		DoubleArrays.divideToSelf(expectedXSquared, data.length);
		return new double[][]{expectedX, expectedXSquared};
	}
	
	/**
	 * Computes alpha based on the average variance across data[.][k] 
	 */
	public static double methodOfMomentsSymmetric(double[][] data) {
		Preconditions.checkNotNull(data);
		Preconditions.checkArgument(data.length > 1);
		
		final int K = data[0].length;
		double[][] moments = moments(data, K);
		double[] expectedX = moments[0];
		double[] expectedXSquared = moments[1];

		double[] var = new double[expectedX.length];
		for (int k = 0; k < var.length; k++) {
			var[k] = expectedXSquared[k] - expectedX[k] * expectedX[k];
		}
		
		// Try matching the mean variance first.
		double alpha = matchVariance(K, DoubleArrays.sum(var) / var.length);
		if (alpha <= 0.0) {
			// If that fails, try the median
			alpha = matchVariance(K, DoubleArrays.median(var, false) / var.length);
			if (alpha <= 0.0) {
				// If that fails, try the max (variance is now sorted after call to median)
				alpha = matchVariance(K, var[var.length - 1]);
				if (alpha <= 0.0) {
					throw new IllegalArgumentException("Moment matching failed for provided dataset.");
				}
			}
		}
		
		return alpha;
	}

	private static double matchVariance(final int K, double var) {
		return (K - 1) / (K * K - var) - 1.0;
	}

	public static double logDensitySymmetric(double[] logTheta, double alpha) {
		return -GammaFunctions.logBetaSymmetric(alpha, logTheta.length) + (alpha - 1) * DoubleArrays.sum(logTheta);
	}

  public static double logDensity(double[] logTheta, double[] alpha) {
    Preconditions.checkArgument(logTheta.length==alpha.length);
    double logDensity = -GammaFunctions.logBeta(alpha);
    for (int i=0; i<logTheta.length; i++){
      logDensity += (alpha[i]-1) * logTheta[i];
    }
    return logDensity;
  }

	public static double[] sample(RealVector alpha, RandomGenerator rnd) {
		double[] theta = GammaDistribution.sample(alpha, rnd);
		DoubleArrays.normalizeToSelf(theta);
		return theta;
	}

  public static double[] sampleSymmetric(double alpha, int len, RandomGenerator rnd) {
    double[] alphavec = new double[len];
    Arrays.fill(alphavec, alpha);
    return sample(alphavec, rnd);
  }
  
	public static double[] sample(double[] alpha, RandomGenerator rnd) {
		double[] theta = GammaDistribution.sample(alpha, rnd);
		DoubleArrays.normalizeToSelf(theta);
		return theta;
	}
	
	public static double[][] sample(RealMatrix alphas, RandomGenerator rnd) {
		double[][] theta = GammaDistribution.sample(alphas, rnd);
		for (int i = 0; i < alphas.getRowDimension(); i++) {
			DoubleArrays.normalizeToSelf(theta[i]);
		}
		return theta;
	}

	public static double[][] sample(double[][] alphas, RandomGenerator rnd) {
		double[][] thetas = new double[alphas.length][]; 
		for (int i = 0; i < thetas.length; i++) {
			thetas[i] = sample(alphas[i], rnd); 
		}
		return thetas;
	}
	
	/**
	 * Returns the log of a sample from the specified Dirichlet distribution.
	 * Similar to calling:
	 * <pre>
	 *   double[] theta = sample(alpha, rnd);
	 *   DoubleArrays.logToSelf(theta);
	 * </pre>
	 * EXCEPT that precision is better preserved.
	 **/
	public static double[] logSample(double[] alpha, RandomGenerator rnd) {
		double[] theta = GammaDistribution.sample(alpha, rnd);
		DoubleArrays.logToSelf(theta);
		DoubleArrays.logNormalizeToSelf(theta);
		return theta;
	}
	
	/**
	 * Computes the log of a sample from the specified Dirichlet distribution and stores the result in alpha.
	 * Precision is preserved through a call to logNormalizeToSelf().
	 **/
	public static void logSampleToSelf(double[] alpha, RandomGenerator rnd) {
		GammaDistribution.sampleToSelf(alpha, rnd);
		DoubleArrays.logToSelf(alpha);
		DoubleArrays.logNormalizeToSelf(alpha);
	}	

	/**
	 * Returns the log of a sample from the specified Dirichlet distribution.
	 * Similar to calling:
	 * <pre>
	 *   double[] theta = sample(alpha, rnd);
	 *   DoubleArrays.logToSelf(theta);
	 * </pre>
	 * EXCEPT that precision is better preserved.
	 **/
	public static double[] logSample(RealVector alpha, RandomGenerator rnd) {
		double[] theta = GammaDistribution.sample(alpha, rnd);
		DoubleArrays.logToSelf(theta);
		DoubleArrays.logNormalizeToSelf(theta);
		return theta;
	}
	
	/** Returns an array of logs of samples from the specified Dirichlet distributions.
	 * Similar to calling:
	 * <pre>
	 *   double[][] thetas = sample(alpha, rnd);
	 *   for (int k = 0; k < thetas.length; k++) {
	 *   	DoubleArrays.logToSelf(thetas[k]);
	 *   }
	 * </pre>
	 * EXCEPT that precision is better preserved.
	 **/
	public static double[][] logSample(double[][] alphas, RandomGenerator rnd) {
		double[][] thetas = new double[alphas.length][]; 
		for (int i = 0; i < thetas.length; i++) {
			thetas[i] = logSample(alphas[i], rnd); 
		}
		return thetas;
	}	

	public static void logSampleToSelf(double[][] alphas, RandomGenerator rnd) {
		for (int i = 0; i < alphas.length; i++) {
			logSampleToSelf(alphas[i], rnd);
		}
	}	

	/** Returns an array of logs of samples from the specified Dirichlet distributions.
	 * Similar to calling:
	 * <pre>
	 *   double[][] thetas = sample(alpha, rnd);
	 *   for (int k = 0; k < thetas.length; k++) {
	 *   	DoubleArrays.logToSelf(thetas[k]);
	 *   }
	 * </pre>
	 * EXCEPT that precision is better preserved.
	 **/
	public static double[][] logSample(RealMatrix alphas, RandomGenerator rnd) {
		double[][] thetas = new double[alphas.getRowDimension()][]; 
		for (int i = 0; i < thetas.length; i++) {
			thetas[i] = logSample(alphas.getRowVector(i), rnd); 
		}
		return thetas;
	}

	public static double[][] sample(RealVector alpha, int numSamples, RandomGenerator rnd) {
		return sample(RealMatrices.repeatRow(alpha, numSamples), rnd);
	}

  public static double[] logSample(double alpha, int dimension, RandomGenerator rnd) {
    double[] theta = new double[dimension];
    for (int i = 0; i < theta.length; i++) {
        theta[i] = GammaDistribution.sample(alpha, rnd); 
    }
    DoubleArrays.logToSelf(theta);
    DoubleArrays.logNormalizeToSelf(theta);
    return theta;
  }

  public static void sampleToSelf(double[] alpha, RandomGenerator rnd) {
    for (int i = 0; i < alpha.length; i++) {
      alpha[i] = GammaDistribution.sample(alpha[i], rnd);
    }
    DoubleArrays.normalizeToSelf(alpha);
  }

  public static void sampleToSelf(double[][] alphas, RandomGenerator rnd) {
    for (int i = 0; i < alphas.length; i++) {
      sampleToSelf(alphas[i], rnd);
    }
  }

}
