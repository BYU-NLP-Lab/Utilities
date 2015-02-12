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

/**
 * @author rah67
 *
 */
public class DirichletMultinomialMLEOptimizableTest {

	private static final Delta delta = Delta.delta(0.1);
	
	private static double[] solve(double[][] data, double[] startPoint, boolean inPlace){

		DirichletMultinomialMLEOptimizable o = DirichletMultinomialMLEOptimizable.newOptimizable(data, inPlace);
		
		double tolerance = 1e-6;
		IterativeOptimizer optimizer = new IterativeOptimizer(ConvergenceCheckers.relativePercentChange(tolerance));
		ValueAndObject<double[]> optimum = optimizer.optimize(o, ReturnType.HIGHEST, true, startPoint);
		
		return optimum.getObject();
	}
	
	/**
	 * Test method for {@link edu.byu.nlp.stats.DirichletMLEOptimizable#computeNext(double[])}.
	 */
	@Test
	public void testComputeNextInPlace() {
		// problem
		boolean inPlace = true;
		double[] alpha = new double[]{ 1,2,3,4 };
		final double[][] data = DirichletTestUtils.sampleMultinomialDataset(alpha,10000,1000,new MersenneTwister(1));
		double[] startPoint = new double[]{9,4,1,7};
		
		// solution
		double[] optimum = solve(data, startPoint, inPlace);
		
		// Ensure that it was performed in place
		assertThat(startPoint).isSameAs(optimum);
		
		// Ensure that we recovered the original parameters
		assertThat(optimum).isEqualTo(alpha, delta);
		
	}

	/**
	 * Test method for {@link edu.byu.nlp.stats.DirichletMLEOptimizable#computeNext(double[])}.
	 */
	@Test
	public void testComputeNextNotInPlace() {
		// problem
		boolean inPlace = false;
		double[] alpha = new double[]{ 1,2,3,4 };
		final double[][] data = DirichletTestUtils.sampleMultinomialDataset(alpha,10000,1000,new MersenneTwister(1));
		DirichletMultinomialMLEOptimizable o = DirichletMultinomialMLEOptimizable.newOptimizable(data, inPlace);
		double[] startPoint = new double[]{9,4,1,7};
		
		// solutions
		double[] optimum = solve(data, startPoint, inPlace);
		
		// Ensure that it was NOT performed in place (no changes should have happened)
		assertThat(startPoint).isEqualTo(new double[]{9,4,1,7});
		
		// Ensure that we recovered the original parameters
		assertThat(optimum).isEqualTo(alpha, delta);
	}


	/**
	 * An interesting property of the Dirichlet-Multinomial is that  
	 * the MLE estimate is the same whether you see data spread 
	 * across many draws or in a single draw. 
	 */
	@Test
	public void testCompetingData() {
		boolean inPlace = true;
		double[] startPoint = new double[]{9,4,1};

		double[][] data1 = new double[][]{
				{3,28,2},
				{1,39,4},
				{4,25,3}};
		double[] optimum1 = solve(data1, startPoint, inPlace);
		
		double[][] data2 = new double[][]{{8,92,9}};
		double[] optimum2 = solve(data2, startPoint, inPlace);

		assertThat(optimum1).isEqualTo(optimum2, delta);
		
	}
}
