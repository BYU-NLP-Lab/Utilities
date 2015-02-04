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
public class SymmetricDirichletMultinomialMLEOptimizable implements Optimizable<Double> {

	private final double[][] data;
	private final double[] perIDataSums;
	private final int N;
	private final int K;
	private double gammaA;
	private double gammaB;

	/**
	 * @param gammaA the first parameter of a gamma hyperprior over the dirichlet 
	 * @param gammaB the second parameter of a gamma hyperprior over the dirichlet
	 */
	public static SymmetricDirichletMultinomialMLEOptimizable newOptimizable(double[][] data, double gammaA, double gammaB) {
		return new SymmetricDirichletMultinomialMLEOptimizable(data,gammaA,gammaB);
	}
	
	private SymmetricDirichletMultinomialMLEOptimizable(double[][] data, double gammaA, double gammaB) {
		Preconditions.checkNotNull(data, "invalid data: "+data);
		Preconditions.checkArgument(data.length>0, "invalid data: "+data);
		Preconditions.checkArgument(data[0].length>0, "invalid data: "+data);
		this.gammaA=gammaA;
		this.gammaB=gammaB;
		this.data = data;
		this.N = data.length;
		this.K = data[0].length;
		this.perIDataSums = Matrices.sumOverSecond(data);
	}
	
	/** {@inheritDoc} */
	@Override
	public ValueAndObject<Double> computeNext(Double alpha) {
		
		double numerator = computeNumerator(data,alpha,N,K) + gammaA - 1;
		double denominator = computeDenominator(perIDataSums,alpha,K) + gammaB;
		double ratio = numerator / denominator; 
		double newalpha = alpha *= ratio;
		
		double value = computeLogLikelihood(data, alpha, perIDataSums, N, K);
		return new ValueAndObject<Double>(value, newalpha);
	}

	private static double computeNumerator(double[][] data, double alpha, int N, int K) {
		double total = 0;
		for (int k=0; k<K; k++){
			for (int i=0; i<N; i++){
				total += Gamma.digamma(data[i][k] + alpha);
			}
		}
		total -= Gamma.digamma(alpha) * N * K; // pulled out of the loop for efficiency
		return total;
	}
	
	private static double computeDenominator(double[] perIDataSums, double alpha, double K) {
		double total = 0;
		double alphaK = alpha*K;
		for (int i=0; i<perIDataSums.length; i++){
			total += Gamma.digamma(perIDataSums[i] + alphaK);
		}
		total -= Gamma.digamma(alphaK) * perIDataSums.length;
		total *= K;
		return total;
	}

	/**
	 * Equation 53 from http://research.microsoft.com/en-us/um/people/minka/papers/dirichlet/minka-dirichlet.pdf
	 */
	private static double computeLogLikelihood(double[][] data, double alpha, double[] perIDataSums, int N, int K) {

		double alphaK = alpha*K;
		
		// this comes from the denominator of the second term
		// which can be factored out of the inner loop into 
		// prod_i prod_k (1/gamma(alpha_k))
		double logGammaOfAlpha = Gamma.logGamma(alpha);
		double llik = -logGammaOfAlpha * N * K;
		
		
		for (int i=0; i<N; i++){
			// first  term numerator
			llik += Gamma.logGamma(alphaK);
			// first term denominator
			llik -= Gamma.logGamma(perIDataSums[i] + alphaK);
			// second term
			for (int k=0; k<K; k++){
				// second term numerator
				llik += Gamma.logGamma(data[i][k] + alpha);
				// second term denominator (factored out and precomputed)
			}
		}
		
		return llik;
	}
	

}