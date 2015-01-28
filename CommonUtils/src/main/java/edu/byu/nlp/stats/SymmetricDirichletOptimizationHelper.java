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

import org.apache.commons.math3.special.Gamma;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import edu.byu.nlp.util.DoubleArrays;

/**
 * @author  rah67
 */
public class SymmetricDirichletOptimizationHelper {

	private static final Logger logger = LoggerFactory.getLogger(SymmetricDirichletOptimizationHelper.class);
	
	private final int K;
	private final int N;
	private final double sumOfLogThetas;

	public static SymmetricDirichletOptimizationHelper newHelper(double[][] data) {
		Preconditions.checkNotNull(data);
		Preconditions.checkArgument(data.length > 0);
		
		double sum = 0.0;
		for (double[] theta : data) {
			sum += DoubleArrays.sum(theta);
		}
		return new SymmetricDirichletOptimizationHelper(data[0].length, data.length, sum);
	}

	private SymmetricDirichletOptimizationHelper(int k, int n, double sumOfLogThetas) {
		K = k;
		N = n;
		this.sumOfLogThetas = sumOfLogThetas;
	}

	public double getsumOfLogThetas() {
		return sumOfLogThetas;
	}

	public int getN() {
		return N;
	}

	public int getK() {
		return K;
	}

	public double valueAt(double alpha) {
		Preconditions.checkArgument(alpha > 0.0, "alpha must be strictly greater than zero; was %s", alpha);
		double value = N * (Gamma.logGamma(K * alpha) - K * Gamma.logGamma(alpha)) + (alpha - 1) * sumOfLogThetas; 
		logger.debug(String.format("valueAt(%f) = %f", alpha, value));
		return value;
	}
	
	public double firstDerivativeAt(double alpha) {
		Preconditions.checkArgument(alpha > 0.0, "alpha must be strictly greater than zero; was %s", alpha);
		double derivative = N * K * (Gamma.digamma(K * alpha) - Gamma.digamma(alpha)) + sumOfLogThetas;
		logger.debug(String.format("derivativeAt(%f) = %f", alpha, derivative));
		return derivative;
	}
	
	public double secondDerivativeAt(double alpha) {
		Preconditions.checkArgument(alpha > 0.0, "alpha must be strictly greater than zero; was %s", alpha);
		double secondDerivative = N * K * (K * Gamma.trigamma(K * alpha) - Gamma.trigamma(alpha));
		logger.debug(String.format("secondDerivativeAt(%f) = %f", alpha, secondDerivative));
		return secondDerivative;
	}
	
}