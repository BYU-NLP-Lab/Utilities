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
package edu.byu.nlp.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;

import edu.byu.nlp.math.Math2;

/**
 * @author rah67
 *
 */
public class DoubleArrays {

	private DoubleArrays() { }

  /**
   * @returns null if arr is of size 0; otherwise 
   * the indices of the top n elements descending value order  
   */
  public static List<Integer> argMaxList(int topn, final double arr[]) {
    if (arr==null || arr.length==0){
      return null;
    }
    Map<Integer,Double> map = Maps.newHashMap();
    for (int i=0; i<arr.length; i++){
      map.put(i, arr[i]);
    }
    return Counters.argMaxList(map.entrySet(), topn, null);
  }
  
	/**
	 * Finds the index of the maximum element in the specified array.
	 * 
	 * @see edu.byu.nlp.util.DoubleArrays#argMax(double[], int, int)
	 *  
	 * @return -1 if arr is of size 0; otherwise, the index of the max element
	 */
	public static int argMax(final double... arr) {
		return DoubleArrays.argMax(arr, 0, arr.length);
	}

	/**
	 * Finds the index of the maximum element in the subarray with range [startIndex, endIndex).
	 * If endIndex is greater than arr.length, the subarray stops at arr.length. The index that is
	 * returned is relative to index; the absolute index can be obtained as follows.
	 * {@code
	 * int absolute = argMax(arr, startIndex, endIndex) + startIndex;
	 * }
	 * 
	 * @throws IllegalArgumentException if len < 0
	 * @throws IndexOutOfBoundsException if index < 0, index >= arr.length, or endIndex < 0
	 * 
	 * @return -1 if the subarray is empty; otherwise, the index of the max element
	 */
	public static int argMax(final double[] arr, final int startIndex, final int endIndex) {
		Preconditions.checkNotNull(arr);
		Preconditions.checkElementIndex(startIndex, arr.length);
		Preconditions.checkArgument(startIndex <= endIndex, "End index (%d) is less than start index (%d)",
		        endIndex, startIndex);
	
		int stop = Math.min(endIndex, arr.length);
		if (stop == startIndex) {
			return -1;
		}
		
		int argMax = startIndex;
		double max = arr[startIndex];
		for (int i = startIndex + 1; i < stop; i++) {
			if (arr[i] > max) {
				max = arr[i];
				argMax = i;
			}
		}
		return argMax - startIndex;
	}

	/**
	 * Swaps two elements in an array.
	 * 
	 * @see edu.byu.nlp.util.DoubleArrays#swap(double[], int, int, int)
	 * 
	 * @throws IndexOutOfBoundsException if arr.length <= (index1 or index2) < 0 
	 */
	public static void swap(final double[] arr, int index1, int index2) {
		DoubleArrays.swap(arr, 0, index1, index2);
	}

	/**
	 * Swaps two elements in a subarray that begins at index startIndex. index1 and index2 are relative to startIndex.
	 *  
	 * @throws IndexOutOfBoundsException if arr.length <= (startIndex + index1 or startIndex + index2) < 0 
	 */
	public static void swap(final double[] arr, int startIndex, int index1, int index2) {
		Preconditions.checkNotNull(arr);
		Preconditions.checkPositionIndex(startIndex + index1, arr.length);
		Preconditions.checkPositionIndex(startIndex + index2, arr.length);
	
		double tmp = arr[startIndex + index1];
		arr[startIndex + index1] = arr[startIndex + index2];
		arr[startIndex + index2] = tmp;
	}

	public static void subtractToSelf(final double[] arr, double val) {
		for (int i = 0; i < arr.length; i++) {
			arr[i] -= val;
		}
	}

	/**
	 * Subtracts arr2 from arr1 and stores the result directly in arr1. Equivalent to:
	 * <p>{@code
	 *     substractFromSelf(arr1, 0, arr2, 0, Math.min(arr1.length, arr2.length))
	 * }
	 * 
	 * @throws IllegalArgumentException if arr2.length < arr1.length
	 */
	public static void subtractToSelf(final double[] arr1, final double[] arr2) {
		Preconditions.checkArgument(arr2.length >= arr1.length);
		
		if (arr1.length == 0 || arr2.length == 0) {
			return;
		}
		
		DoubleArrays.subtractToSelf(arr1, 0, arr2, 0, Math.min(arr1.length, arr2.length));
	}

	/**
	 * Subtracts arr2 from arr1 and stores the result directly in arr1 starting at the specified positions in the
	 * arrays. The subtraction is computed element-by-element, left-to-right, so care should be taken when arr1 == arr2.
	 * Copying stops before the specified length would extend past the end of either array.
	 * 
	 * @throws IndexOutOfBoundsException if startIndex1 >= arr1.length or startIndex2 >= arr2.length
	 * @return the actual number of elements copied
	 */
	public static int subtractToSelf(final double[] arr1, int startIndex1, final double[] arr2, int startIndex2,
			int length) {
		Preconditions.checkNotNull(arr1);
		Preconditions.checkNotNull(arr2);
		Preconditions.checkElementIndex(startIndex1, arr1.length);
		Preconditions.checkElementIndex(startIndex2, arr2.length);
		
		for (int index1 = startIndex1, index2 = startIndex2;
				index1 < arr1.length && index2 < arr2.length;
				index1++, index2++) {
			arr1[index1] -= arr2[index2];
		}
		return Math.min(Math.min(arr1.length - startIndex1, arr2.length - startIndex2), length);
	}

	/**
	 * Subtracts alpha * arr2 from arr1 and stores the result directly in arr1. If arr2 is larger than arr1
	 * the extra values are ignored.
	 * 
	 * @throws IllegalArgumentException if arr2.length < arr1.length
	 */
	public static void subtractToSelfWeighted(final double[] arr1, double alpha, final double[] arr2) {
		Preconditions.checkNotNull(arr1);
		Preconditions.checkNotNull(arr2);
		Preconditions.checkArgument(arr2.length >= arr1.length);
		
		for (int index = 0; index < arr1.length; index++) {
			arr1[index] -= alpha * arr2[index];
		}
	}

	/**
	 * Adds alpha * arr2 to arr1 and stores the result directly in arr1. If arr2 is larger than arr1
	 * the extra values are ignored.
	 * 
	 * @throws IllegalArgumentException if arr2.length < arr1.length
	 */
	public static void addToSelfWeighted(final double[] arr1, double alpha, final double[] arr2) {
		Preconditions.checkNotNull(arr1);
		Preconditions.checkNotNull(arr2);
		Preconditions.checkArgument(arr2.length >= arr1.length);
		
		for (int index = 0; index < arr1.length; index++) {
			arr1[index] += alpha * arr2[index];
		}
	}

	/**
	 * Computes alpha * arr1 + beta * arr2 and stores the result in arr1.
	 */
	public static void addToSelfWeighted(final double[] arr1, double alpha, final double[] arr2, double beta) {
		Preconditions.checkNotNull(arr1);
		Preconditions.checkNotNull(arr2);
		Preconditions.checkArgument(arr2.length >= arr1.length);
	
		for (int index = 0; index < arr1.length; index++) {
			arr1[index] = alpha * arr1[index] + beta * arr2[index];
		}
	}

	public static double sum(final double[] arr) {
		double sum = 0.0;
		for (double d : arr) {
			sum += d;
		}
		return sum;
	}

	public static void normalizeToSelf(final double[] arr) {
		double sum = sum(arr);
		for (int i = 0; i < arr.length; i++) {
			arr[i] /= sum;
		}
	}

	/** More cache friendly than separate calls **/
	public static void normalizeAndLogToSelf(final double[] arr) {
		double sum = sum(arr);
		for (int i = 0; i < arr.length; i++) {
			arr[i] = Math.log(arr[i] / sum);
		}
	}

	public static void expToSelf(final double[] arr) {
		for (int i = 0; i < arr.length; i++) {
			arr[i] = Math.exp(arr[i]);
		}
	}

	public static double[] exp(final double[] arr) {
    double[] clone = arr.clone();
    expToSelf(clone);
    return clone;
  }

	public static double[] log(final double[] arr) {
		double[] clone = arr.clone();
		logToSelf(clone);
		return clone;
	}
	
	public static void logToSelf(final double[] arr) {
		for (int i = 0; i < arr.length; i++) {
			arr[i] = Math.log(arr[i]);
		}
	}

	public static double logSum(final double... v) {
		double sumExponentiatedDiffs = 0.0;
		int argMax = argMax(v);
		double max = v[argMax];
		for (int i = 0; i < argMax; i++) {
			double diff = v[i] - max;
			if (diff >= Math2.LOGSUM_THRESHOLD) {
				sumExponentiatedDiffs += Math.exp(diff);
			}
		}
		for (int i = argMax + 1; i < v.length; i++) {
			double diff = v[i] - max;
			if (diff >= Math2.LOGSUM_THRESHOLD) {
				sumExponentiatedDiffs += Math.exp(diff);
			}
		}
		return max + Math.log(1.0 + sumExponentiatedDiffs);
	}

	public static double logNormalizeToSelf(final double[] v) {
		double logSum = logSum(v);
		subtractToSelf(v, logSum);
		return logSum;
	}

	public static void addToSelf(final double[] a, double[] b) {
		Preconditions.checkNotNull(a);
		Preconditions.checkNotNull(b);
		Preconditions.checkArgument(a.length == b.length);
		
		for (int i = 0; i < a.length; i++) {
			a[i] += b[i];
		}
	}

	/** Returns the last element **/
	public static double last(final double[] arr) {
		return arr[arr.length - 1];
	}

	public static double max(final double[] arr) {
		return arr[argMax(arr)];
	}

	public static void multiplyToSelf(final double[] arr, double m) {
		for (int i = 0; i < arr.length; i++) {
			arr[i] *= m;
		}
	}

	public static void divideToSelf(final double[] arr, double m) {
		for (int i = 0; i < arr.length; i++) {
			arr[i] /= m;
		}
	}

	public static double[] subtract(final double[] arr, double x) {
		double[] copy = new double[arr.length];
		for (int i = 0; i < copy.length; i++) {
			copy[i] = arr[i] - x;
		}
		return copy;
	}

	public static void addToSelf(final double[] arr, double x) {
		for (int i = 0; i < arr.length; i++) {
			arr[i] += x;
		}
	}

	/** No checking is done to assure this is a proper distribution **/
	public static double entropy(final double[] dist) {
		double entropy = 0.0;
		for (double p : dist) {
			if (p > 0.0) {
				entropy -= p * Math.log(p);
			}
		}
		return entropy;
	}

	public static void pointwiseDivideToSelf(final double[] a, int offsetA, double[] b, int offsetB, int length) {
		Preconditions.checkNotNull(a);
		Preconditions.checkNotNull(b);
		Preconditions.checkElementIndex(offsetA, a.length);
		Preconditions.checkElementIndex(offsetB, b.length);
		
		for (int i = 0; i < length; i++) {
			a[offsetA + i] /= b[offsetB + i];
		}
	}

	public static void addToSelf(final double[] a, final int offsetA, final double[] b, final int offsetB,
			final int length) {
		Preconditions.checkNotNull(a);
		Preconditions.checkNotNull(b);
		Preconditions.checkElementIndex(offsetA, a.length);
		Preconditions.checkElementIndex(offsetB, b.length);
	
		for (int i = 0; i < length; i++) {
			a[offsetA + i] += b[offsetB + i];
		}
	}

	public static void addRangeToSelf(final double[] a, final int offsetA, final double[] b, final int offsetB,
			final int length) {
		for (int i = 0; i < length; i++) {
			a[offsetA + i] += b[offsetB + i];
		}
	}

	public static double sumOfRange(final double[] a, int start, final int length) {
		double sum = 0;
		for (int i = 0; i < length; i++) {
			sum += a[start + i];
		}
		return sum;
	}

	public static void addToRangeToSelf(final double[] a, final int start, final int length, final double value) {
		for (int i = 0; i < length; i++) {
			a[start + i] += value;
		}
	}

	public static double[] constant(double fill, int size) {
		double[] arr = new double[size];
		Arrays.fill(arr, fill);
		return arr;
	}

	public static boolean hasNaN(double[] arr) {
		for (double x : arr) {
			if (Double.isNaN(x)) {
				return true;
			}
		}
		return false;
	}
	
	public static double[] fromDoubleCollection(Collection<Double> coll) {
		double[] arr = new double[coll.size()];
		int i = 0;
		for(Double d : coll) {
			arr[i++] = d;
		}
		return arr;
	}
	
	public static double median(double[] arr, boolean preserve) {
		Preconditions.checkNotNull(arr);
		Preconditions.checkArgument(arr.length > 0);
		
		if (preserve) {
			arr = arr.clone();
		}
		Arrays.sort(arr);
		if (arr.length % 2 == 0) {
			return arr[arr.length / 2];
		}
		return (arr[arr.length / 2] + arr[arr.length / 2 + 1]) / 2.0;
	}
	
	public static double mean(double[] arr) {
		Preconditions.checkNotNull(arr);
		return DoubleArrays.sum(arr) / arr.length;
	}

    /**
     * Root mean squared error
     */
    public static double rmse(double[] a, double[] b) {
        Preconditions.checkNotNull(a);
        Preconditions.checkNotNull(b);
        Preconditions.checkArgument(a.length == b.length);
        
        double sumSquaredError = 0.0;
        for (int i = 0; i < a.length; i++) {
            double diff = a[i] - b[i];
            sumSquaredError += diff * diff;
        }
        return Math.sqrt(sumSquaredError / a.length);
    }

    /**
     * Returns true iff each element of coeff1 is equal to coeff2 with the specified tolerance.
     */
    public static boolean equals(double[] coeff1, double[] coeff2, double tolerance) {
      Preconditions.checkNotNull(coeff1);
      Preconditions.checkNotNull(coeff2);
      if (coeff1.length != coeff2.length) {
        return false;
      }
      for (int i = 0; i < coeff1.length; i++) {
        if (!Math2.doubleEquals(coeff1[i], coeff2[i], tolerance)) {
          return false;
        }
      }
      return true;
    }

    /**
     * Returns an array of the specified size with all elements initialized to value.
     */
    public static double[] of(double value, int size) {
      double[] arr = new double[size];
      Arrays.fill(arr, value);
      return arr;
    }

    public static String toString(double[] arr) {
      StringBuilder builder = new StringBuilder();
      builder.append('[');
      for (int i=0; i<arr.length; i++){
        builder.append(arr[i]);
        if (i!=arr.length-1){
          builder.append(',');
        }
      }
      builder.append(']');
      return builder.toString();
    }


    public static double[] parseDoubleArray(String str){
		return new Gson().fromJson(str, double[].class);
    }
    
    public static Iterator<Double> iterator(double[] array){
      List<Double> list = new ArrayList<Double>(array.length);
      for (double item: array){
        list.add(item);
      }
      return list.iterator();
    }

	public static double dotProduct(double[] a, double[] b) {
	      Preconditions.checkNotNull(a);
	      Preconditions.checkNotNull(b);
	      Preconditions.checkArgument(a.length==b.length);
	      double total = 0;
	      for (int i=0; i<a.length; i++){
	        total += a[i] * b[i];
	      }
	      return total;
	}
	
    public static double dotProduct(double[] a, int[] b) {
      Preconditions.checkNotNull(a);
      Preconditions.checkNotNull(b);
      Preconditions.checkArgument(a.length==b.length);
      double total = 0;
      for (int i=0; i<a.length; i++){
        total += a[i] * b[i];
      }
      return total;
    }
    
    public static int[] round(double[] a){
      int[] result = new int[a.length]; 
      for (int i=0; i<a.length; i++){
        result[i] = (int) Math.round(a[i]);
      }
      return result;
    }
    
    public static double[] fromInts(int[] ints){
      double[] result = new double[ints.length];
      for (int i=0; i<ints.length; i++){
        result[i] = ints[i];
      }
      return result;
    }

    public static double[] fromList(List<Double> list) {
      double[] retval = new double[list.size()];
      for (int i=0; i<list.size(); i++){
        retval[i] = list.get(i);
      }
      return retval;
    }
    
    public static double min(double[] arr){
    	double min = Double.POSITIVE_INFINITY;
    	for (int i=0; i<arr.length; i++){
    		min = Math.min(min, arr[i]);
    	}
    	return min;
    }

    /**
     * Extend one array with a number of elements
     */
	public static double[] extend(double[] a, double ... b) {
		Preconditions.checkNotNull(a);
		Preconditions.checkNotNull(b);
		double[] result = new double[a.length+b.length];
		System.arraycopy(a, 0, result, 0, a.length);
		System.arraycopy(b, 0, result, a.length, b.length);
		return result;
	}

	public static List<Double> asList(double[] annotatorRates) {
		List<Double> result = Lists.newArrayListWithCapacity(annotatorRates.length);
		for (int i=0; i<annotatorRates.length; i++){
			result.add(annotatorRates[i]);
		}
		return result;
	}

    
}
