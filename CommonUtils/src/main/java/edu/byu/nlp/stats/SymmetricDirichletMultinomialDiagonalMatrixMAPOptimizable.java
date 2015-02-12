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
import edu.byu.nlp.util.Pair;

/**
 * @author plf1
 * 
 * Optimizable for computing the MLE of Dirichlet-multinomial distributed data 
 * when a whole matrix of hyperparameters is tied together such there are are two 
 * values: the diagonal and the off-diagonal. This is derived by adapting 
 * the log likelihood in equation (53) of 
 * http://research.microsoft.com/en-us/um/people/minka/papers/dirichlet/minka-dirichlet.pdf
 * to additionally sum over data for other distributions, and then taking the derivative
 * wrt each tied parameter in the same way as equation 54. 
 * The result is that the fixed point algorithm in equation 
 * 55 gets additional sums in both the numerator and denominator, and some 
 * terms simplify since many of the alpha terms are tied to the same value.  
 * 
 * Note that you can summarize all of the data for each distribution into a single 
 * row because in general the MLE/MAP of a dirichlet-multinomial are the same 
 * whether data is spread across multiple sparse observations or is condensed into a 
 * single dense observation. 
 * 
 * If you pass in a single row of data then this reduces to solving the parameter of a 
 * single symmetric Dirichlet-multinomial.
 * 
 * We also add a gamma prior like section 3.1 of http://arxiv.org/pdf/1205.2662.pdf
 * For now we've assumed that both the diag and off-diag param share the same 
 * gamma prior.
 * 
 * Technically the data should be int[][], but we are allowing a small generalization here 
 * in case data have been scaled to fractional counts.
 */
public class SymmetricDirichletMultinomialDiagonalMatrixMAPOptimizable implements Optimizable<Pair<Double,Double>> {

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
	public static SymmetricDirichletMultinomialMatrixMAPOptimizable newOptimizable(double[][] data, double gammaA, double gammaB) {
		return new SymmetricDirichletMultinomialMatrixMAPOptimizable(data,gammaA,gammaB);
	}
	
	private SymmetricDirichletMultinomialMatrixMAPOptimizable(double[][] data, double gammaA, double gammaB) {
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
	public ValueAndObject<Pair<Double,Double>> computeNext(Pair<Double,Double> params) {
		
		// diag
		double diag = params.getFirst();
		double diagNumerator = computeDiagNumerator(data,diag,N,K) + gammaA - 1; 
		double diagDenominator = computeDiagDenominator(perIDataSums,diag,K) + gammaB;
		double diagRatio = diagNumerator / diagDenominator; 
		double newDiag = diag *= diagRatio;
		
		// off diag
		double offdiag = params.getSecond();
		double offDiagNumerator = computeOffDiagNumerator(data,offdiag,N,K) + gammaA - 1; 
		double offDiagDenominator = computeOffDiagDenominator(perIDataSums,offdiag,K) + gammaB;
		double offDiagRatio = offDiagNumerator / offDiagDenominator; 
		double newOffDiag = offdiag *= offDiagRatio;
		
		double value = computeLogLikelihood(data, diag, offdiag, perIDataSums, N, K);
		return new ValueAndObject<Pair<Double,Double>>(value, Pair.of(newDiag, newOffDiag));
	}


	private double computeOffDiagDenominator(double[] perIDataSums, double offdiag, int K) {
		// TODO Auto-generated method stub
		return 0;
	}

	private double computeOffDiagNumerator(double[][] data, double offdiag, int N, int K) {
		// TODO Auto-generated method stub
		return 0;
	}

	private double computeDiagDenominator(double[] perIDataSums, double diag, int K) {
		// TODO Auto-generated method stub
		return 0;
	}

	private double computeDiagNumerator(double[][] data, double diag, int N, int K) {
		// TODO Auto-generated method stub
		return 0;
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
	 * adapted to a matrix of data (just more outer sums)
	 */
	private double computeLogLikelihood(double[][] data, double diag, double offdiag, double[] perIDataSums, int N, int K) {
		double llik = 0;
		// diagonal contribution
		
	
		// off-diag contribution
		
	
		double alphaK = alpha*K;
		
		// this comes from the denominator of the second term
		// which can be factored out of the inner loop into 
		// prod_i prod_k (1/gamma(alpha_k))
		double logGammaOfAlpha = Gamma.logGamma(alpha);
		llik -= logGammaOfAlpha * N * K;
		
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