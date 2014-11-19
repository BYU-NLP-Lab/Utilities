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

import edu.byu.nlp.math.optimize.IterativeOptimizer.Optimizable;
import edu.byu.nlp.math.optimize.ValueAndObject;

/**
 * Newton-Raphson update for computing the MLE of Symmetric0Dirichlet distributed data.
 * 
 * http://research.microsoft.com/en-us/um/people/minka/papers/dirichlet/minka-dirichlet.pdf
 */
public class SymmetricDirichletMLENROptimizable implements Optimizable<Double> {

	private final SymmetricDirichletOptimizationHelper helper;
	
	public static SymmetricDirichletMLENROptimizable newOptimizable(double[][] data) {
		SymmetricDirichletOptimizationHelper helper = SymmetricDirichletOptimizationHelper.newHelper(data);
		return new SymmetricDirichletMLENROptimizable(helper);
	}
	
	private SymmetricDirichletMLENROptimizable(SymmetricDirichletOptimizationHelper helper) {
		this.helper = helper;
	}
	
	/** {@inheritDoc} */
	@Override
	public ValueAndObject<Double> computeNext(Double alphaD) {
		double alpha = alphaD.doubleValue();
		double nextAlpha = alpha - helper.firstDerivativeAt(alpha) / helper.secondDerivativeAt(alpha);
		if (nextAlpha <= 0.0) {
			throw new IllegalStateException("Newton-Raphson update failed; alpha = " + nextAlpha);
		}
		double value = helper.valueAt(nextAlpha);
		return new ValueAndObject<Double>(value, nextAlpha);
	}
}