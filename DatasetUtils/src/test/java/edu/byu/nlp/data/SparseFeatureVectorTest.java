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
package edu.byu.nlp.data;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

import org.fest.assertions.Delta;
import org.junit.Test;

import edu.byu.nlp.data.types.SparseFeatureVector;
import edu.byu.nlp.dataset.BasicSparseFeatureVector;

/**
 * @author rah67
 *
 */
public class SparseFeatureVectorTest {

//	/**
//	 * Test method for {@link edu.byu.nlp.data.SparseFeatureVector#dotProduct(double[])}.
//	 */
//	@Test
//	public void testDotProduct() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link edu.byu.nlp.data.SparseFeatureVector#copy()}.
//	 */
//	@Test
//	public void testCopy() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link edu.byu.nlp.data.SparseFeatureVector#transformValues(edu.byu.nlp.data.SparseFeatureVectors.ValueFunction)}.
//	 */
//	@Test
//	public void testTransformValues() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link edu.byu.nlp.data.SparseFeatureVector#visitIndices(edu.byu.nlp.data.SparseFeatureVector.IndexVisitor)}.
//	 */
//	@Test
//	public void testVisitIndices() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link edu.byu.nlp.data.SparseFeatureVector#visitSparseEntries(edu.byu.nlp.data.SparseFeatureVector.EntryVisitor)}.
//	 */
//	@Test
//	public void testVisitSparseEntries() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link edu.byu.nlp.data.SparseFeatureVector#sparseEntries()}.
//	 */
//	@Test
//	public void testSparseEntries() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link edu.byu.nlp.data.SparseFeatureVector#valueComparator()}.
//	 */
//	@Test
//	public void testValueComparator() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link edu.byu.nlp.data.SparseFeatureVector#sum()}.
//	 */
//	@Test
//	public void testSum() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link edu.byu.nlp.data.SparseFeatureVector#preMultiplyAndAddTo(double[], double[][])}.
//	 */
//	@Test
//	public void testPreMultiplyAndAddToDoubleArrayDoubleArrayArray() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link edu.byu.nlp.data.SparseFeatureVector#preMultiplyAndAddTo(double[], double[], int)}.
//	 */
//	@Test
//	public void testPreMultiplyAndAddToDoubleArrayDoubleArrayInt() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link edu.byu.nlp.data.SparseFeatureVector#preMultiplyAsColumnAndAddTo(double[][], double[])}.
//	 */
//	@Test
//	public void testPreMultiplyAsColumnAndAddToDoubleArrayArrayDoubleArray() {
//		fail("Not yet implemented");
//	}

	/**
	 * Test method for {@link edu.byu.nlp.data.SparseFeatureVector#preMultiplyAsColumnAndAddTo(double[], double[])}.
	 */
	@Test
	public void testPreMultiplyAsColumnAndAddToDoubleArrayDoubleArray() {
		double[] matrix = new double[] {
				 1.0,  2.0,  3.0,
				 4.0,  5.0,  6.0,
				 7.0,  8.0,  9.0,
				10.0, 11.0, 12.0 };

		double[] init = new double[]{ -1.1, 0.1, 2.3 };
		
		SparseFeatureVector v = new BasicSparseFeatureVector(new int[]{1, 2}, new double[]{0.1, -1.0});
		double[] expected = new double[] {
				init[0] + 4.0 * 0.1 + 7.0 * -1.0,
				init[1] + 5.0 * 0.1 + 8.0 * -1.0,
				init[2] + 6.0 * 0.1 + 9.0 * -1.0 };

		v.preMultiplyAsColumnAndAddTo(matrix, init);

		assertThat(init).isEqualTo(expected, Delta.delta(1e-8));
	}

//	/**
//	 * Test method for {@link edu.byu.nlp.data.SparseFeatureVector#preMultiplyAsColumn(double[], double[])}.
//	 */
//	@Test
//	public void testPreMultiplyAsColumn() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link edu.byu.nlp.data.SparseFeatureVector#addTo(double[])}.
//	 */
//	@Test
//	public void testAddTo() {
//		fail("Not yet implemented");
//	}

	/**
	 * Test method for {@link edu.byu.nlp.data.SparseFeatureVector#addToRow(double[], int, int)}.
	 */
	@Test
	public void testAddToColumn() {
		double[] matrix = new double[] {
			 1.0,  2.0,  3.0,
			 4.0,  5.0,  6.0,
			 7.0,  8.0,  9.0,
			10.0, 11.0, 12.0 };
		double[] expected = matrix.clone();

		SparseFeatureVector v = new BasicSparseFeatureVector(new int[]{1, 2}, new double[]{0.1, -1.0});
		v.addToRow(matrix, 1, 3);
		
		expected[4] += 0.1;
		expected[7] += -1.0;
		assertThat(matrix).isEqualTo(expected, Delta.delta(1e-10));
	}

//	/**
//	 * Test method for {@link edu.byu.nlp.data.SparseFeatureVector#subtractFrom(double[])}.
//	 */
//	@Test
//	public void testSubtractFrom() {
//		fail("Not yet implemented");
//	}

}
