/**
 * Copyright 2011 Brigham Young University
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
package edu.byu.nlp.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.google.common.collect.Lists;

/**
 * Unit tests for Arrays2.
 * 
 * @author rah67
 *
 */
public class DoubleArraysTest {

	/**
	 * Test method for {@link edu.byu.nlp.util.DoubleArrays#argMax(double[], int, int)}.
	 */
	@Test
	public void argMaxReturnsNegativeOneWhenLengthIsZero() {
		double[] arr;
		
		// One Element
		arr = new double[]{0};
		assertEquals(-1, DoubleArrays.argMax(arr, 0, 0));

		// Two elements
		arr = new double[]{1, 2};
		assertEquals(-1, DoubleArrays.argMax(arr, 0, 0));
		assertEquals(-1, DoubleArrays.argMax(arr, 1, 1));
		
		arr = new double[]{2, 1};
		assertEquals(-1, DoubleArrays.argMax(arr, 0, 0));
		assertEquals(-1, DoubleArrays.argMax(arr, 1, 1));

		// Three elements
		arr = new double[]{1, 2, 3};
		assertEquals(-1, DoubleArrays.argMax(arr, 0, 0));
		assertEquals(-1, DoubleArrays.argMax(arr, 1, 1));
		assertEquals(-1, DoubleArrays.argMax(arr, 2, 2));
		
		arr = new double[]{2, 1, 3};
		assertEquals(-1, DoubleArrays.argMax(arr, 0, 0));
		assertEquals(-1, DoubleArrays.argMax(arr, 1, 1));
		assertEquals(-1, DoubleArrays.argMax(arr, 2, 2));
	}

	/**
	 * Test method for {@link edu.byu.nlp.util.DoubleArrays#argMax(double[], int, int)}.
	 */
	@Test
	public void argMaxThrowsCorrectExceptionsWhenPreconditionsArentMet() {
		// Null array
		double[] arr = null;
		try {
			DoubleArrays.argMax(arr, 0, 0);
			fail("Null array should throw IllegalArgumentException");
		} catch (NullPointerException expected) {}
		
		// Argument "index" is out-of-bounds
		arr = new double[]{1};
		try {
			DoubleArrays.argMax(arr, 1, 0);
			fail("Index argument should throw IndexOutOfBoundsException");
		} catch (IndexOutOfBoundsException expected) {}

		// Argument "endIndex" is negative
		arr = new double[]{1};
		try {
			DoubleArrays.argMax(arr, 0, -1);
			fail("Negative endIndex argument should throw IllegalArgumentException");
		} catch (IllegalArgumentException expected) {}

		// endIndex can be out-of-bounds; arr.length is used as an upper bound;
		arr = new double[]{1};
		DoubleArrays.argMax(arr, 0, 2);

		// Combo length and index out-of-bounds together, is okay
		arr = new double[]{1, 2};
		assertEquals(0, DoubleArrays.argMax(arr, 1, 2));
	}
	
	/**
	 * Test method for {@link edu.byu.nlp.util.DoubleArrays#argMax(double[], int, int)}.
	 */
	@Test
	public void testArgMaxWithOneItem() {
		double[] arr = {2};
		assertEquals(0, DoubleArrays.argMax(arr));
	}

	/**
	 * Test method for {@link edu.byu.nlp.util.DoubleArrays#argMax(double[])}.
	 */
	@Test
	public void testArgMaxWithTwoItems() {
		double[] arr;
		
		arr = new double[]{1, 2};
		assertEquals(1, DoubleArrays.argMax(arr));
		
		arr = new double[]{2, 1};
		assertEquals(0, DoubleArrays.argMax(arr));
	}

	/**
	 * Test method for {@link edu.byu.nlp.util.DoubleArrays#argMax(double[])}.
	 */
	@Test
	public void testArgMaxWithThreeItems() {
		double[] arr;
		arr = new double[]{1, 2, 3};
		assertEquals(2, DoubleArrays.argMax(arr));
		arr = new double[]{2, 1, 3};
		assertEquals(2, DoubleArrays.argMax(arr));
		arr = new double[]{2, 3, 1};
		assertEquals(1, DoubleArrays.argMax(arr));
		arr = new double[]{3, 1, 2};
		assertEquals(0, DoubleArrays.argMax(arr));
		arr = new double[]{3, 2, 1};
		assertEquals(0, DoubleArrays.argMax(arr));
	}

	/**
	 * Test method for {@link edu.byu.nlp.util.DoubleArrays#argMax(double[])}.
	 */
	@Test
	public void argMaxTieIsArbitrary() {
		double[] arr;
		arr = new double[]{1, 2, 2};
		assertTrue(DoubleArrays.argMax(arr) == 1 || DoubleArrays.argMax(arr) == 2);
	}
	
	/**
	 * Test method for {@link edu.byu.nlp.util.DoubleArrays#argMax(double[], int, int)}.
	 */
	@Test
	public void testArgMaxWithSubArray() {
		double[] arr;
		
		// Length extends to the end
		arr = new double[]{5, 1, 2, 3};
		assertEquals(2, DoubleArrays.argMax(arr, 1, 4));
		
		arr = new double[]{5, 2, 1, 3, 6};
		assertEquals(2, DoubleArrays.argMax(arr, 1, 4));
		arr = new double[]{5, 2, 3, 1, 6};
		assertEquals(1, DoubleArrays.argMax(arr, 1, 4));
		arr = new double[]{5, 3, 1, 2, 6};
		assertEquals(0, DoubleArrays.argMax(arr, 1, 4));
		arr = new double[]{5, 3, 2, 1, 6};
		assertEquals(0, DoubleArrays.argMax(arr, 1, 4));
		
		// Check the different indices
		assertEquals(0, DoubleArrays.argMax(arr, 0, 1));
		assertEquals(0, DoubleArrays.argMax(arr, 1, 2));
		assertEquals(0, DoubleArrays.argMax(arr, 2, 3));
		assertEquals(0, DoubleArrays.argMax(arr, 3, 4));
		assertEquals(0, DoubleArrays.argMax(arr, 4, 5));
	}
	
	/**
	 * Test method for {@link edu.byu.nlp.util.DoubleArrays#swap(double[], int, int, int)}.
	 */
	@Test
	public void swapThrowsCorrectExceptionsWhenPreconditionsArentMet() {
		try {
			DoubleArrays.swap(null, 0, 0);
			fail("Passing null array should result in null pointer exception");
		} catch (NullPointerException expected) {}
		
		double arr[] = new double[]{1, 2};

		// index1 is out-of-bounds
		try {
			DoubleArrays.swap(arr, 0, 2, 1);
		} catch (IndexOutOfBoundsException expected) { }
		try {
			DoubleArrays.swap(arr, 1, 1, 0);
		} catch (IndexOutOfBoundsException expected) { }

		// Negative index1
		try {
			DoubleArrays.swap(arr, 2, -1, 0);
		} catch (IndexOutOfBoundsException expected) { }
		
		// index2 is out-of-bounds
		try {
			DoubleArrays.swap(arr, 0, 0, 2);
		} catch (IndexOutOfBoundsException expected) { }
		try {
			DoubleArrays.swap(arr, 1, 0, 1);
		} catch (IndexOutOfBoundsException expected) { }
		
		// Negative index1
		try {
			DoubleArrays.swap(arr, 2, 0, -1);
		} catch (IndexOutOfBoundsException expected) { }
		
		// length is out-of-bounds
		try {
			DoubleArrays.swap(arr, 2, 0, 0);
		} catch (IndexOutOfBoundsException expected) { }

		// length is out-of-bounds
		try {
			DoubleArrays.swap(arr, -1, 2, 1);
		} catch (IndexOutOfBoundsException expected) { }
	}

	/**
	 * Test method for {@link edu.byu.nlp.util.DoubleArrays#swap(double[], int, int, int)}.
	 */
	@Test
	public void testSwap() {
		double arr[] = new double[]{1, 2, 3, 4};
		
		// Self swap does nothing
		DoubleArrays.swap(arr, 2, 1, 1);
		assertArrayEquals(arr, arr, 0.0);
		DoubleArrays.swap(arr, 1, 2, 2);
		assertArrayEquals(arr, arr, 0.0);
		
		// Typical swaps
		arr = new double[]{1, 2, 3, 4};
		DoubleArrays.swap(arr, 2, 0, 1);
		assertArrayEquals(arr, new double[]{1, 2, 4, 3}, 0.0);
		arr = new double[]{1, 2, 3, 4};
		DoubleArrays.swap(arr, 2, 1, 0);
		assertArrayEquals(arr, new double[]{1, 2, 4, 3}, 0.0);
		
		arr = new double[]{1, 2, 3, 4};
		DoubleArrays.swap(arr, 1, 1, 2);
		assertArrayEquals(arr, new double[]{1, 2, 4, 3}, 0.0);
		arr = new double[]{1, 2, 3, 4};
		DoubleArrays.swap(arr, 1, 2, 1);
		assertArrayEquals(arr, new double[]{1, 2, 4, 3}, 0.0);
	}

	/**
	 * Test method for {@link edu.byu.nlp.util.DoubleArrays#subtractToSelf(double[], int, double[], int, int)}.
	 */
	@Test
	public void testSubstractToSelf() {
		// Check for NPE
		try {
			DoubleArrays.subtractToSelf(null, 0, new double[]{1, 2}, 0, 2);
			fail();
		} catch (NullPointerException expected) { }
		try {
			DoubleArrays.subtractToSelf(new double[]{1, 2}, 0, null, 0, 2);
			fail();
		} catch (NullPointerException expected) { }
		
		// Check for index-out-of-bounds
		try {
			DoubleArrays.subtractToSelf(new double[]{1, 2}, 3, new double[]{1, 2}, 0, 2);
			fail();
		} catch (IndexOutOfBoundsException expected) { }
		try {
			DoubleArrays.subtractToSelf(new double[]{1, 2}, 0, new double[]{1, 2}, 3, 2);
			fail();
		} catch (IndexOutOfBoundsException expected) { }
		try {
			DoubleArrays.subtractToSelf(new double[]{}, 0, new double[]{}, 0, 0);
			fail();
		} catch (IndexOutOfBoundsException expected) { }
		try {
			DoubleArrays.subtractToSelf(new double[]{1, 2}, 0, new double[]{}, 0, 0);
			fail();
		} catch (IndexOutOfBoundsException expected) { }
		try {
			DoubleArrays.subtractToSelf(new double[]{}, 0, new double[]{1, 2}, 0, 0);
			fail();
		} catch (IndexOutOfBoundsException expected) { }
		
		// Zero length
		assertEquals(0, DoubleArrays.subtractToSelf(new double[]{1, 2}, 0, new double[]{1, 2}, 0, 0));
		
		double[] arr1 = new double[] {1, 2, 3, 4, 5};
		double[] arr2 = new double[] {5, 4, 3, 2, 1};
		
		// Test with different offsets
		double[] copy = Arrays.copyOf(arr1, arr1.length);
		assertEquals(3, DoubleArrays.subtractToSelf(copy, 1, arr2, 2, 3));
		assertArrayEquals(new double[]{1, -1, 1, 3, 5}, copy, 1e-14);
		
		// Subtract from self should be all zeroes
		assertEquals(5, DoubleArrays.subtractToSelf(copy, 0, copy, 0, copy.length));
		assertArrayEquals(new double[]{0, 0, 0, 0, 0}, copy, 1e-14);
		
		// Different tests to make sure the correct length is reported in different scenarios
		assertEquals(5, DoubleArrays.subtractToSelf(arr1, 0, arr2, 0, 5));
		assertEquals(4, DoubleArrays.subtractToSelf(arr1, 1, arr2, 0, 5));
		assertEquals(4, DoubleArrays.subtractToSelf(arr1, 0, arr2, 1, 5));
		assertEquals(4, DoubleArrays.subtractToSelf(arr1, 1, arr2, 1, 5));
		assertEquals(2, DoubleArrays.subtractToSelf(arr1, 0, arr2, 3, 5));
		assertEquals(2, DoubleArrays.subtractToSelf(arr1, 3, arr2, 0, 5));
		assertEquals(1, DoubleArrays.subtractToSelf(arr1, 4, arr2, 4, 5));
	}

	/**
	 * Test method for {@link edu.byu.nlp.util.DoubleArrays#addToSelfWeighted(double[], double, double[])}.
	 */
	@Test
	public void testSubstractToSelfWeighted() {
		// Check for NPE
		try {
			DoubleArrays.subtractToSelfWeighted(null, 1.0, new double[]{1, 2});
			fail();
		} catch (NullPointerException expected) { }
		try {
			DoubleArrays.subtractToSelfWeighted(new double[]{1, 2}, 1.0, null);
			fail();
		} catch (NullPointerException expected) { }
		
		// arr2 is smaller than arr1
		try {
			DoubleArrays.subtractToSelfWeighted(new double[]{1, 2}, 1.0, new double[]{1});
			fail();
		} catch (IllegalArgumentException expected) { }
		
		// zero length arrays ok
		DoubleArrays.subtractToSelfWeighted(new double[]{}, 1.0, new double[]{});
		DoubleArrays.subtractToSelfWeighted(new double[]{}, 1.0, new double[]{1});

		// Equal sized arrays
		double[] arr1 = new double[] {1, 2, 3, 4, 5};
		double[] arr2 = new double[] {5, 4, 3, 2, 1};
		double[] original = Arrays.copyOf(arr1, arr1.length);
		DoubleArrays.subtractToSelfWeighted(arr1, 3.14, arr2);
		assertArrayEquals(new double[]{
				original[0] - 3.14 * arr2[0],
				original[1] - 3.14 * arr2[1],
				original[2] - 3.14 * arr2[2],
				original[3] - 3.14 * arr2[3],
				original[4] - 3.14 * arr2[4]}, arr1, 1e-14);
		
		// Array2 is bigger
		arr1 = new double[] {1, 2, 3};
		original = Arrays.copyOf(arr1, arr1.length);
		DoubleArrays.subtractToSelfWeighted(arr1, 3.14, arr2);
		assertArrayEquals(new double[]{
				original[0] - 3.14 * arr2[0],
				original[1] - 3.14 * arr2[1],
				original[2] - 3.14 * arr2[2]}, arr1, 1e-14);
	}

	/**
	 * Test method for {@link edu.byu.nlp.util.DoubleArrays#subtractToSelfWeighted(double[], double, double[])}.
	 */
	@Test
	public void testAddToSelfWeighted() {
		// Check for NPE
		try {
			DoubleArrays.addToSelfWeighted(null, 1.0, new double[]{1, 2});
			fail();
		} catch (NullPointerException expected) { }
		try {
			DoubleArrays.addToSelfWeighted(new double[]{1, 2}, 1.0, null);
			fail();
		} catch (NullPointerException expected) { }
		
		// arr2 is smaller than arr1
		try {
			DoubleArrays.addToSelfWeighted(new double[]{1, 2}, 1.0, new double[]{1});
			fail();
		} catch (IllegalArgumentException expected) { }
		
		// zero length arrays ok
		DoubleArrays.addToSelfWeighted(new double[]{}, 1.0, new double[]{});
		DoubleArrays.addToSelfWeighted(new double[]{}, 1.0, new double[]{1});

		// Equal sized arrays
		double[] arr1 = new double[] {1, 2, 3, 4, 5};
		double[] arr2 = new double[] {5, 4, 3, 2, 1};
		double[] original = Arrays.copyOf(arr1, arr1.length);
		DoubleArrays.addToSelfWeighted(arr1, 3.14, arr2);
		assertArrayEquals(new double[]{
				original[0] + 3.14 * arr2[0],
				original[1] + 3.14 * arr2[1],
				original[2] + 3.14 * arr2[2],
				original[3] + 3.14 * arr2[3],
				original[4] + 3.14 * arr2[4]}, arr1, 1e-14);
		
		// Array2 is bigger
		arr1 = new double[] {1, 2, 3};
		original = Arrays.copyOf(arr1, arr1.length);
		DoubleArrays.addToSelfWeighted(arr1, 3.14, arr2);
		assertArrayEquals(new double[]{
				original[0] + 3.14 * arr2[0],
				original[1] + 3.14 * arr2[1],
				original[2] + 3.14 * arr2[2]}, arr1, 1e-14);
	}
	

  @Test
  public void testParseIntArray(){
    double[] arr = DoubleArrays.parseDoubleArray("[1,2,3]");
    assertArrayEquals(new double[]{1,2,3}, arr, 1e-14);

    arr = DoubleArrays.parseDoubleArray("1,2,3");
    assertArrayEquals(new double[]{1,2,3}, arr, 1e-14);

    arr = DoubleArrays.parseDoubleArray("1.1234123,2.1234,3.2345523");
    assertArrayEquals(new double[]{1.1234123,2.1234,3.2345523}, arr, 1e-14);
  }
	
  @Test
  public void testArgmaxList(){

    double [] arr = new double[]{1, 2, 4, 3};

    assertEquals(Lists.newArrayList(2,3,1,0), DoubleArrays.argMaxList(0, arr));
    assertEquals(Lists.newArrayList(2,3,1,0), DoubleArrays.argMaxList(-1, arr));

    assertEquals(Lists.newArrayList(2), DoubleArrays.argMaxList(1, arr));
    assertEquals(Lists.newArrayList(2,3), DoubleArrays.argMaxList(2, arr));
  }
  
  @Test
  public void testExtend(){
	  double[] a = new double[]{1,4,12,8,2};
	  double[] b = DoubleArrays.extend(a,2,7,4,67.23);
	  Assertions.assertThat(b).isEqualTo(new double[]{1,4,12,8,2,2,7,4,67.23});
  }
  
}
