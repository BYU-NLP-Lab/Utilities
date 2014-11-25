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
package edu.byu.nlp;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.linear.SparseRealMatrix;
import org.fest.assertions.Assertions;
import org.junit.Assert;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * @author rah67
 *
 */
public class MoreAsserts {
	
	private MoreAsserts() { }
	
	public static void assertMatrixEquals(double[][] actual, double[][] expected, double tol) {
		if (expected == actual) {
			return;
		}
		
		if (expected.length != actual.length) {
			Assert.fail("Number of rows differ in Matrix");
		}
		
		for (int i = 0; i < expected.length; i++) {
			assertArrayEquals(expected[i], actual[i], tol);
		}
	}

	public static void assertArrayEquals(double[] actual, double[] expected, double tol){
	  for (int i = 0; i < expected.length; i++) {
      Assert.assertEquals(expected[i], actual[i], tol);
    } 
	}
	
  public static void assertMatrixEquals(int[][] actual, int[][] expected) {
    if (expected == actual) {
      return;
    }
    
    if (expected.length != actual.length) {
      Assert.fail("Number of rows differ in Matrix");
    }
    
    for (int i = 0; i < expected.length; i++) {
      Assert.assertArrayEquals(expected[i], actual[i]);
    }
  }

	public static <E extends Comparable<? super E>> void assertEqualsUnordered(Collection<? extends E> expected,
			Collection<? extends E> actual) {
		List<E> sortedExpected = Lists.newArrayList(expected);
		Collections.sort(sortedExpected);
		List<E> sortedActual = Lists.newArrayList(actual);
		Collections.sort(sortedActual);
		Assert.assertEquals(sortedExpected, sortedActual);
	}

	public static void assertSparseMatricesEqual(final SparseRealMatrix actual, final SparseRealMatrix expected){
		Preconditions.checkNotNull(actual);
		Preconditions.checkNotNull(expected);
		Preconditions.checkArgument(actual.getRowDimension()==expected.getRowDimension());
		
		// check densely--this doesn't need to be fast
		for (int r=0; r<actual.getRowDimension(); r++){
			for (int c=0; c<actual.getColumnDimension(); c++){
				double act = actual.getEntry(r, c);
				double exp = expected.getEntry(r, c); 
				if (act!=exp){
					System.err.println("actual["+r+"]["+c+"]="+act+" does not match expected["+r+"]["+c+"]="+exp);
				}
				Assertions.assertThat(act).isEqualTo(exp);
			}
		}
	}
}
