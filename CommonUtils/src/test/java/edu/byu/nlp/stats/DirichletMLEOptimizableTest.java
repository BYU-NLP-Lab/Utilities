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

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.special.Gamma;
import org.fest.assertions.Delta;
import org.junit.Test;

import edu.byu.nlp.math.optimize.ValueAndObject;
import edu.byu.nlp.util.DoubleArrays;

/**
 * @author rah67
 *
 */
public class DirichletMLEOptimizableTest {

	private static final Delta delta = Delta.delta(1e-10);
	
	/**
	 * Test method for {@link edu.byu.nlp.stats.DirichletMLEOptimizable#computeNext(double[])}.
	 */
	@Test
	public void testComputeNextInPlace() {
		final double[][] data = DirichletTestUtils.sampleDataset();
		
		DirichletMLEOptimizable o = DirichletMLEOptimizable.newOptimizable(data, true);
		double[] alpha = new double[]{ 2.0, 3.0, 4.0 };
		// Guard against bugs in the test
		assertThat(alpha.length).isEqualTo(data[0].length);
		
		double[] alphaCopy = alpha.clone();
		ValueAndObject<double[]> vao = o.computeNext(alphaCopy);
		
		// Ensure that it was performed in place
		assertThat(vao.getObject()).isSameAs(alphaCopy);
		
		// Ensure that the update was computed correctly
		RealVector expected = DirichletMLEOptimizableTest.newtonRaphsonUpdate(data, alpha);
		assertThat(alphaCopy).isEqualTo(expected.toArray(), delta);
		
		// TODO : check value
	}

	/**
	 * Test method for {@link edu.byu.nlp.stats.DirichletMLEOptimizable#computeNext(double[])}.
	 */
	@Test
	public void testComputeNextNotInPlace() {
		final double[][] data = DirichletTestUtils.sampleDataset();
		
		DirichletMLEOptimizable o = DirichletMLEOptimizable.newOptimizable(data, false);
		double[] alpha = new double[]{ 2.0, 3.0, 4.0 };
		// Guard against bugs in the test
		assertThat(alpha.length).isEqualTo(data[0].length);
		
		ValueAndObject<double[]> vao = o.computeNext(alpha);
		
		// Ensure that it was not performed in place
		assertThat(vao.getObject()).isNotSameAs(alpha);
	}

	/**
	 * Computes a Newton-Raphson update in-place to alpha.
	 */
	private static RealVector newtonRaphsonUpdate(final double[][] data, double[] alpha) {
		// We'll compute the gold-standard value the "long" way (taking the inverse of the Hessian)
		RealMatrix hessian = new Array2DRowRealMatrix(alpha.length, alpha.length);
		for (int r = 0; r < hessian.getRowDimension(); r++) {
			for (int c = 0; c < hessian.getColumnDimension(); c++){
				hessian.addToEntry(r, c, data.length * Gamma.trigamma(DoubleArrays.sum(alpha)));
				if (r == c) {
					hessian.addToEntry(r, c, -data.length * Gamma.trigamma(alpha[r]));
				}
			}
		}
		RealVector derivative = new ArrayRealVector(alpha.length);
		for (int k = 0; k < alpha.length; k++) {
			derivative.setEntry(k, data.length * (Gamma.digamma(DoubleArrays.sum(alpha)) - Gamma.digamma(alpha[k])));
			for (double[] theta : data) {
				derivative.addToEntry(k, theta[k]);
			}
		}
		
		RealMatrix hessianInverse = new LUDecomposition(hessian).getSolver().getInverse();
		RealVector negDiff = hessianInverse.preMultiply(derivative);
		negDiff.mapMultiplyToSelf(-1.0);
		
		RealVector expected = new ArrayRealVector(alpha, true);
		return expected.add(negDiff);
	}

}
