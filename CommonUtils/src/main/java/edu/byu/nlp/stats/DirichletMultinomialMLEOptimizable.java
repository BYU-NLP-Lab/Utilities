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

import com.google.common.base.Preconditions;

import edu.byu.nlp.math.optimize.IterativeOptimizer.Optimizable;
import edu.byu.nlp.math.optimize.ValueAndObject;
import edu.byu.nlp.util.DoubleArrays;
import edu.byu.nlp.util.Matrices;

/**
 * @author plf1
 * 
 * Optimizable for computing the MLE of Dirichlet-multinomial distributed data.
 * 
 * Technically the data should be int[][], but we are allowing a small generalization here 
 * in case data have been scaled to fractional counts.
 * 
 * http://research.microsoft.com/en-us/um/people/minka/papers/dirichlet/minka-dirichlet.pdf
 * note: If we wanted a gamma prior MAP it would look sort of like section 3.1 in http://arxiv.org/pdf/1205.2662.pdf
 * except that one is a symmetric dirichlet
 */
public class DirichletMultinomialMLEOptimizable implements Optimizable<double[]> {

	private final boolean inPlace;
	private final double[][] data;
	private final double[] perIDataSums;
	private final int N;
	private final int K;
	
	public static DirichletMultinomialMLEOptimizable newOptimizable(double[][] data, boolean inPlace) {
		return new DirichletMultinomialMLEOptimizable(data, inPlace);
	}
	
	private DirichletMultinomialMLEOptimizable(double[][] data, boolean inPlace) {
		Preconditions.checkNotNull(data, "invalid data: "+data);
		Preconditions.checkArgument(data.length>0, "invalid data: "+data);
		Preconditions.checkArgument(data[0].length>0, "invalid data: "+data);
		this.data = data;
		this.N = data.length;
		this.K = data[0].length;
		this.perIDataSums = Matrices.sumOverSecond(data);
		this.inPlace = inPlace;
	}
	
	/** {@inheritDoc} */
	@Override
	public ValueAndObject<double[]> computeNext(double[] alpha) {
		Preconditions.checkArgument(alpha.length==K, "the starting point (k="+alpha.length+") has different dimensions from the data! (k="+K+")");
		if (!inPlace) {
			alpha = alpha.clone();
		}
		double alphaSum = DoubleArrays.sum(alpha);
		double denominator = computeDenominator(perIDataSums,alphaSum);
		for (int k = 0; k < K; k++) {
			double numerator = computeNumerator(data,alpha,k,N);
			double ratio = numerator / denominator; 
			alpha[k] *= ratio;
		}
		
		double value = computeLogLikelihood(data, alpha, alphaSum, perIDataSums, N, K);
		return new ValueAndObject<double[]>(value, alpha);
	}

	private static double computeNumerator(double[][] data, double[] alpha, int k, int N) {
		double total = 0;
		for (int i=0; i<N; i++){
			total += Gamma.digamma(data[i][k] + alpha[k]);
		}
		total -= Gamma.digamma(alpha[k]) * N; // pulled out of the loop for efficiency
		return total;
	}
	
	private static double computeDenominator(double[] perIDataSums, double alphaSum) {
		double total = 0;
		for (int i=0; i<perIDataSums.length; i++){
			total += Gamma.digamma(perIDataSums[i] + alphaSum);
		}
		total -= Gamma.digamma(alphaSum) * perIDataSums.length;
		return total;
	}

	/**
	 * Equation 53 from http://research.microsoft.com/en-us/um/people/minka/papers/dirichlet/minka-dirichlet.pdf
	 */
	private static double computeLogLikelihood(double[][] data, double[] alpha, double alphaSum, double[] perIDataSums, int N, int K) {
		
		// this comes from the denominator of the second term
		// which can be factored out of the inner loop into 
		// prod_i prod_k (1/gamma(alpha_k))
		double[] logGammaOfAlpha = alpha.clone();
		for (int k=0; k<K; k++){
			logGammaOfAlpha[k] = Gamma.logGamma(logGammaOfAlpha[k]);
		}
		double logGammaOfAlphaSum = DoubleArrays.sum(logGammaOfAlpha);
		double llik = -logGammaOfAlphaSum * N;
		
		
		for (int i=0; i<N; i++){
			// first  term numerator
			llik += Gamma.logGamma(alphaSum);
			// first term denominator
			llik -= Gamma.logGamma(perIDataSums[i] + alphaSum);
			// second term
			for (int k=0; k<K; k++){
				// second term numerator
				llik += Gamma.logGamma(data[i][k] + alpha[k]);
				// second term denominator (factored out and precomputed)
			}
		}
		
		return llik;
	}
	

}