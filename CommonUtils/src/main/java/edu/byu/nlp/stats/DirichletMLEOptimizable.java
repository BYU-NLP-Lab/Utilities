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

import edu.byu.nlp.math.optimize.IterativeOptimizer.Optimizable;
import edu.byu.nlp.math.optimize.ValueAndObject;
import edu.byu.nlp.util.DoubleArrays;
import edu.byu.nlp.util.Matrices;

/**
 * Optimizable for computing the MLE of Dirichlet distributed data.
 * 
 * http://research.microsoft.com/en-us/um/people/minka/papers/dirichlet/minka-dirichlet.pdf
 */
public class DirichletMLEOptimizable implements Optimizable<double[]> {

	private final double[] sumTheta;
	private final int N;
	private final boolean inPlace;
	
	public static DirichletMLEOptimizable newOptimizable(double[][] data, boolean inPlace) {
		return new DirichletMLEOptimizable(Matrices.sumOverFirst(data), data.length, inPlace);
	}
	
	private DirichletMLEOptimizable(double[] sumTheta, int N, boolean inPlace) {
		this.sumTheta = sumTheta;
		this.N = N;
		this.inPlace = inPlace;
	}
	
	/** {@inheritDoc} */
	@Override
	public ValueAndObject<double[]> computeNext(double[] alpha) {
		if (!inPlace) {
			alpha = alpha.clone();
		}
		
		double alphaSum = DoubleArrays.sum(alpha);
		double[] g = computeG(alpha, alphaSum);
		double[] q = computeQ(alpha);
		double b = computeB(g, q, N * Gamma.trigamma(alphaSum));
		for (int k = 0; k < alpha.length; k++) {
			alpha[k] -= (g[k] - b) / q[k];
		}
		double value = computeValue(alpha);
		return new ValueAndObject<double[]>(value, alpha);
	}
	
	private double computeValue(double[] alpha) {
		double value = Gamma.logGamma(DoubleArrays.sum(alpha));
		for (int k = 0; k < alpha.length; k++) {
			value -= Gamma.logGamma(alpha[k]);
		}
		value *= N;
		
		for (int k = 0; k < alpha.length; k++) {
			value += (alpha[k] - 1) * sumTheta[k];
		}
		
		return value;
	}
	
	private double[] computeG(double[] alpha, double alphaSum) {
		double digammaAlphaSum = Gamma.digamma(alphaSum);
		double[] g = new double[alpha.length];
		for (int k = 0; k < g.length; k++) {
			g[k] = N * (digammaAlphaSum - Gamma.digamma(alpha[k])) + sumTheta[k];
		}
		return g;
	}

	private double[] computeQ(double[] alpha) {
		double[] q = new double[alpha.length];
		for (int k = 0; k < q.length; k++) {
			q[k] = -N * Gamma.trigamma(alpha[k]);
		}
		return q;
	}

	private double computeB(double[] g, double[] q, double z) {
		double num = 0.0;
		for (int k = 0; k < g.length; k++) {
			num += g[k] / q[k];
		}
		double denom = 1.0 / z;
		for (int k = 0; k < q.length; k++) {
			denom += 1.0 / q[k];
		}
		return num / denom;
	}

}