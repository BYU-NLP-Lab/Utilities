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

	private final double[][][] data;
	private final int J;
	private final int K;
	private double gammaA;
	private double gammaB;

	/**
	 * @param gammaA the first parameter of a gamma hyperprior over the dirichlet 
	 * @param gammaB the second parameter of a gamma hyperprior over the dirichlet
	 */
	public static SymmetricDirichletMultinomialDiagonalMatrixMAPOptimizable newOptimizable(double[][][] data, double gammaA, double gammaB) {
		return new SymmetricDirichletMultinomialDiagonalMatrixMAPOptimizable(data,gammaA,gammaB);
	}
	
	private SymmetricDirichletMultinomialDiagonalMatrixMAPOptimizable(double[][][] data, double gammaA, double gammaB) {
		Preconditions.checkNotNull(data, "invalid data: "+data);
		Preconditions.checkArgument(data.length>0, "invalid data: "+data);
		Preconditions.checkArgument(data[0].length>0, "invalid data: "+data);
		Preconditions.checkArgument(gammaA*gammaB>0,"gammaA and gammaB must either both be valid (>0) or both invalid (<=0)");
		this.gammaA=gammaA;
		this.gammaB=gammaB;
		this.data = data;
		this.J = data.length;
		this.K = data[0].length;
		Preconditions.checkArgument(data[0][0].length==this.K,"parameter matrices must be square");
	}
	
	/** {@inheritDoc} */
	@Override
	public ValueAndObject<Pair<Double,Double>> computeNext(Pair<Double,Double> params) {
		double alphaDiag = params.getFirst();
		double alphaOffdiag = params.getSecond();
		
		// diag
		double diagNumerator = computeDiagNumerator(data,alphaDiag,J,K); 
		if (gammaA>0){
			diagNumerator += (gammaA - 1) / alphaDiag; // optional prior
		}
		double diagDenominator = computeDiagDenominator(data,alphaDiag,alphaOffdiag,J,K);
		if (gammaB>0){
			diagDenominator += gammaB; // optional prior
		}
		double diagRatio = diagNumerator / diagDenominator; 
		double newDiag = alphaDiag * diagRatio;
		
		// off diag
		double offDiagNumerator = computeOffDiagNumerator(data,alphaOffdiag,J,K); 
		if (gammaA>0){
			offDiagNumerator += (gammaA - 1) / alphaOffdiag; // optional prior
		}
		double offDiagDenominator = computeOffDiagDenominator(data,alphaDiag,alphaOffdiag,J,K);
		if (gammaB>0){
			offDiagDenominator += gammaB; // optional prior
		}
		double offDiagRatio = offDiagNumerator / offDiagDenominator; 
		double newOffDiag = alphaOffdiag * offDiagRatio;
		
		double value = computeLogLikelihood(data, alphaDiag, alphaOffdiag, gammaA, gammaB, J, K);
		return new ValueAndObject<Pair<Double,Double>>(value, Pair.of(newDiag, newOffDiag));
	}

	private double computeDiagNumerator(double[][][] data, double bdiag, int J, int K) {
		double total = 0;
		for (int j=0; j<J; j++){
			for (int k=0; k<K; k++){
				total += Gamma.digamma(data[j][k][k] + bdiag);
			}
		}
		
		total -= J * K * Gamma.digamma(bdiag);
				
		return total;
	}

	private double computeDiagDenominator(double[][][] data, double bdiag, double offdiag, int J, int K) {
		double total = 0;
		double alphasum = bdiag + (K-1)*offdiag;
		
		for (int j=0; j<J; j++){
			double[] nj = Matrices.sumOverSecond(data[j]);
			for (int k=0; k<K; k++){
				total += Gamma.digamma(nj[k] + alphasum);
			}
		}
		
		total -= J * K * Gamma.digamma(alphasum);
		
		return total;
	}

	private double computeOffDiagNumerator(double[][][] data, double offdiag, int J, int K) {
		double total = 0;

		for (int j=0; j<J; j++){
			for (int k=0; k<K; k++){
				for (int kprime=0; kprime<K; kprime++){
					if (k!=kprime){
						total += Gamma.digamma(data[j][k][kprime]+offdiag);
					}
				}
			}
		}
		
		total -= J * K * (K-1) * Gamma.digamma(offdiag);
		return total;
	}

	private double computeOffDiagDenominator(double[][][] data, double diag, double offdiag, int J, int K) {
		double total = 0;
		double alphasum = diag + (K-1)*offdiag;

		for (int j=0; j<J; j++){
			double[] nj = Matrices.sumOverSecond(data[j]);
			for (int k=0; k<K; k++){
				total += Gamma.digamma(nj[k] + alphasum);
			}
		}
		total -= J * K * Gamma.digamma(alphasum);
		return (K-1) * total;
	}


	/**
	 * Equation 53 from http://research.microsoft.com/en-us/um/people/minka/papers/dirichlet/minka-dirichlet.pdf 
	 * adapted to a matrix of data (just more outer sums)
	 */
	private double computeLogLikelihood(double[][][] data, double diag, double offdiag, double gammaA, double gammaB, int J, int K) {
		double llik = 0;
		
		// gamma priors
		if (gammaA>0 && gammaB>0){
			llik += ((gammaA - 1) * Math.log(diag)) - gammaB * diag;
			llik += ((gammaA - 1) * Math.log(offdiag)) - gammaB * offdiag;
		}
		
		// lik
		for (int j=0; j<J; j++){
			double[] nj = Matrices.sumOverSecond(data[j]);
			for (int k=0; k<K; k++){
				double totalAlpha = offdiag*(K-1) + diag;
				// first  term numerator
				llik += Gamma.logGamma(totalAlpha);
				// first term denominator
				llik -= Gamma.logGamma(nj[k] + totalAlpha);
				// second term
				for (int kprime=0; kprime<K; kprime++){
					// second term numerator
					llik += Gamma.logGamma(data[j][k][kprime] + (k==kprime?diag: offdiag));
					// second term denominator (factored out and precomputed)
				}
			}
		}
		
		// second term denominator
		llik -= J * K * Gamma.logGamma(diag);
		llik -= J * K * (K-1) * Gamma.logGamma(offdiag);
		
		return llik;
	}
	

}