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

import static org.fest.assertions.Assertions.assertThat;

import org.apache.commons.math3.special.Gamma;
import org.fest.assertions.Delta;
import org.junit.Test;

import edu.byu.nlp.math.optimize.ValueAndObject;
import edu.byu.nlp.util.DoubleArrays;
import edu.byu.nlp.util.Matrices;

/**
 * @author rah67
 *
 */
public class SymmetricDirichletMLEOptimizableTest {

	private static final Delta delta = Delta.delta(1e-10);
	
	/**
	 * Test method for {@link edu.byu.nlp.stats.SymmetricDirichletMLENROptimizable#computeNext(java.lang.Double)}.
	 */
	@Test
	public void testComputeNext() {
		final double alpha = 2.13;
		
		final double[][] data = DirichletTestUtils.sampleDataset();
		SymmetricDirichletMLENROptimizable o = SymmetricDirichletMLENROptimizable.newOptimizable(data);

		ValueAndObject<Double> vao = o.computeNext(alpha);
		
		// Compute the update that should have happened
		final int N = data.length;
		final int K = data[0].length;
		double sumOfLogThetas = DoubleArrays.sum(Matrices.sumOverFirst(data));
		double value = N * Gamma.logGamma(K * alpha) - N * K * Gamma.logGamma(alpha) + (alpha - 1) * sumOfLogThetas;
		double firstDeriv = N * Gamma.digamma(K * alpha) - N * K * Gamma.digamma(alpha) + sumOfLogThetas;
		double secondDeriv = N * Gamma.trigamma(K * alpha) - N * K * Gamma.trigamma(alpha);
		double nextAlpha = alpha - firstDeriv / secondDeriv;
		
		// Ensure that the update was computed correctly
		assertThat(vao.getValue()).isEqualTo(value, delta);
		assertThat(vao.getObject().doubleValue()).isEqualTo(nextAlpha, delta);
	}

}
