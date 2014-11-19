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
package edu.byu.nlp.math;

import org.apache.commons.math3.special.Gamma;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

import edu.byu.nlp.util.DoubleArrays;

/**
 * @author rah67
 *
 */
public class GammaFunctions {

	private static final double IS_INTEGER_EPS = 1e-10;
	
	@VisibleForTesting
	static final int MANUALLY_COMPUTE_RISING_FACTORIAL_THRESHOLD = 8;
	
	private GammaFunctions() { }

	/**
	 * Computes ln Gamma(x + k) - ln Gamma(x)  
	 */
  public static double logRatioOfGammasByDifference(double x, double k) {
    return logRatioOfGammas(x+k, x);
  }
	
	/**
	 * Computes ln Gamma(numerator) - ln Gamma(denominator).
	 */
	public static double logRatioOfGammas(double numerator, double denominator) {
		// There are potential speed ups when the difference is integral
		double diff = numerator - denominator;
		if (Math2.isIntegral(diff, IS_INTEGER_EPS)) {
		  // When n = numerator - denominator is an integer, then 
		  // Gamma(numerator) / Gamma(denominator) = x * (x + 1) * ... * (x + n - 1)
		  // Which is, by definition the rising factorial of x^(n)
		  return logRisingFactorial(denominator, (int) (Math.round(diff)));
		}
		return Gamma.logGamma(numerator) - Gamma.logGamma(denominator);
	}
	
	private static double logRisingFactorialManual(double x, double k) {
		// A few optimized special cases
		if (k == 0) {
			return 0.0;
		}
		
		// By starting the accumulator here we can avoid one less compare in the for-loop.
		double acc = Math.log(x);
		if (k == 1) {
			return acc;
		}
		
		for (int i = 1; i < k; i++) {
			acc += Math.log(x + i);
		}
		return acc;
	}

	public static double logRisingFactorial(double x, int diff) {
		Preconditions.checkArgument(diff >= 0);
		Preconditions.checkArgument(x >= 0.0);
//    if (diff < -0.0) {
//      throw new IllegalArgumentException("Argument must be positive; was " + diff);
//    }
    
    // We can use a falling factorial when the difference is small.
    if (diff < MANUALLY_COMPUTE_RISING_FACTORIAL_THRESHOLD) {
      return logRisingFactorialManual(x, diff);
    }
    return Gamma.logGamma(x + diff) - Gamma.logGamma(x);
	}

	public static double logBetaSymmetric(double alpha, int dim) {
		return dim * Gamma.logGamma(alpha) - Gamma.logGamma(dim * alpha);
	}

	public static double logBeta(double[] alpha) {
		double sum = Gamma.logGamma(alpha[0]);
		for (int i = 1; i < alpha.length; i++) {
			sum += Gamma.logGamma(alpha[i]);
		}
		return sum - Gamma.logGamma(DoubleArrays.sum(alpha));
	}
}
