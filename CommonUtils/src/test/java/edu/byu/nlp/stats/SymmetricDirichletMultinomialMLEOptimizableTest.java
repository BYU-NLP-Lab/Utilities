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

import org.apache.commons.math3.random.MersenneTwister;
import org.fest.assertions.Delta;
import org.junit.Test;

import edu.byu.nlp.math.optimize.ConvergenceCheckers;
import edu.byu.nlp.math.optimize.IterativeOptimizer;
import edu.byu.nlp.math.optimize.IterativeOptimizer.ReturnType;
import edu.byu.nlp.math.optimize.ValueAndObject;
import edu.byu.nlp.util.DoubleArrays;

/**
 * @author rah67
 *
 */
public class SymmetricDirichletMultinomialMLEOptimizableTest {

	private static final Delta delta = Delta.delta(0.1);
	
	/**
	 * Test method for {@link edu.byu.nlp.stats.DirichletMLEOptimizable#computeNext(double[])}.
	 */
	@Test
	public void testComputeNextInPlace() {

		double alpha = 0.01;
		int K = 5;
		final double[][] data = DirichletTestUtils.sampleMultinomialDataset(DoubleArrays.constant(alpha, K),1000,100,new MersenneTwister(1));
		SymmetricDirichletMultinomialMatrixMAPOptimizable o = SymmetricDirichletMultinomialMatrixMAPOptimizable.newOptimizable(data,2,2);
		
		double tolerance = 1e-6;
		IterativeOptimizer optimizer = new IterativeOptimizer(ConvergenceCheckers.relativePercentChange(tolerance));
		Double startPoint = 7.;
		ValueAndObject<Double> optimum = optimizer.optimize(o, ReturnType.HIGHEST, true, startPoint);
		
		// Ensure that we recovered the original parameters
		assertThat(optimum.getObject()).isEqualTo(alpha, delta);
		
	}

}
