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
import edu.byu.nlp.util.Pair;

/**
 * @author plf1
 *
 */
public class SymmetricDirichletMultinomialDiagonalMatrixMAPOptimizableTest {

	private static final Delta delta = Delta.delta(0.3);
	
	/**
	 * Test method for {@link edu.byu.nlp.stats.DirichletMLEOptimizable#computeNext(double[])}.
	 */
	@Test
	public void testComputeNextInPlace() {

		double diag = 10;
		double offdiag = 1;
		int numDataPoints = 1000;
		int datumSize = 10000;
		int matrixSize = 3;
		final double[][][] data = DirichletTestUtils.sampleDirichletMultinomialMatrixDataset(diag, offdiag, matrixSize , numDataPoints, datumSize, new MersenneTwister(1));
		SymmetricDirichletMultinomialDiagonalMatrixMAPOptimizable o = SymmetricDirichletMultinomialDiagonalMatrixMAPOptimizable.newOptimizable(data,-1,-1);
		
		double tolerance = 1e-10;
		IterativeOptimizer optimizer = new IterativeOptimizer(ConvergenceCheckers.relativePercentChange(tolerance));
		Pair<Double,Double> startPoint = Pair.of(7., 4.2);
		ValueAndObject<Pair<Double,Double>> optimum = optimizer.optimize(o, ReturnType.HIGHEST, true, startPoint);
		
		// Ensure that we recovered the original parameters
		assertThat(optimum.getObject().getFirst()).isEqualTo(diag, delta);
		assertThat(optimum.getObject().getSecond()).isEqualTo(offdiag, delta);
		
	}

}
