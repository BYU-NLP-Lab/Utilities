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
 * Fixed-point iteration for computing the MLE of Symmetric-Dirichlet distributed data.
 * Based on the update to the precision parameter.
 * 
 * http://research.microsoft.com/en-us/um/people/minka/papers/dirichlet/minka-dirichlet.pdf
 */
public class SymmetricDirichletMLEFPOptimizable implements Optimizable<Double> {

	private final double[] meanLogTheta;
	private final int N;
	private final int K;
	
	public static SymmetricDirichletMLEFPOptimizable newOptimizable(double[][] data) {
		Preconditions.checkNotNull(data);
		Preconditions.checkArgument(data.length > 0);
		
		double[] meanLogTheta = Matrices.sumOverFirst(data);
		DoubleArrays.divideToSelf(meanLogTheta, data.length);
		return new SymmetricDirichletMLEFPOptimizable(meanLogTheta, data.length, data[0].length);
	}
	
	private SymmetricDirichletMLEFPOptimizable(double[] meanLogTheta, int N, int K) {
		this.meanLogTheta = meanLogTheta;
		this.N = N;
		this.K = K;
	}
	
	/** {@inheritDoc} */
	@Override
	public ValueAndObject<Double> computeNext(Double alphaD) {
		double alpha = alphaD.doubleValue();
		double s = K * alpha;
		double denom = (K - 1) / s - Gamma.digamma(s) + Gamma.digamma(s / K) ;
		double nextS = (K - 1) / denom;
		double nextAlpha = nextS / K;
		if (nextAlpha <= 0.0) {
			throw new IllegalStateException("Fixed-point update failed; alpha = " + nextAlpha);
		}
		
		double value = N * (Gamma.logGamma(nextS) + nextAlpha * DoubleArrays.sum(meanLogTheta)
				- K * Gamma.logGamma(nextAlpha));
		return new ValueAndObject<Double>(value, nextAlpha);
	}
}