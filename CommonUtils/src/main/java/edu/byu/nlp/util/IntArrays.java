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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.random.RandomGenerator;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gson.Gson;


/**
 * @author rah67
 *
 */
public class IntArrays {

  
	public static long sum(int[] arr) {
		long sum = 0;
		for (int i : arr) {
			sum += i;
		}
		return sum;
	}

	public static int max(int[] counts){
		return counts[argMax(counts)];
	}
	
	public static int argMax(int[] counts) {
		if (counts.length == 0) {
			return -1;
		}
		
		int argMax = 0;
		int max = counts[0];
		for (int i = 1; i < counts.length; i++) {
			if (counts[i] > max) {
				max = counts[i];
				argMax = i;
			}
		}
		return argMax;
	}

	/**
	 * Find the index of the indicated element in the sub-array with range [start, end).
	 */
  public static int indexOf(int element, int[] map, int start, int end) {
    for (int i = start; i < end; i++) {
      if (map[i] == element) {
        return i;
      }
    }
    return -1;
  }

  public static String toString(int[] arr) {
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

  public static int[] parseIntArray(String str){
		return new Gson().fromJson(str, int[].class);
  }
  
  /**
   * Returns a sequence from start (inclusive) to end (exclusive)
   */
  public static int[] sequence(int start, int end){
    Preconditions.checkArgument(start>=0);
    Preconditions.checkArgument(end>=start);
    int[] seq = new int[end-start];
    for (int i=0; i<end-start; i++){
      seq[i] = start+i;
    }
    return seq;
  }
  
  public static int[] shuffled(int[] arr, RandomGenerator rnd){
    // int[] -> List
    List<Integer> tmp = Lists.newArrayListWithCapacity(arr.length);
    for (int i=0; i<arr.length; i++){
      tmp.add(arr[i]);
    } 
    // shuffle
    Collections.shuffle(tmp, new Random(rnd.nextLong()));
    // List -> int[] (wish there were a better way to do this)
    int[] arr2 = new int[tmp.size()];
    for (int i=0; i<tmp.size(); i++){
      arr2[i] = tmp.get(i);
    }
    return arr2;
  }

  public static int[] repeat(int i, int size) {
    int[] arr = new int[size];
    Arrays.fill(arr, i);
    return arr;
  }

  /**
   * convert int[] to List<Integer>
   */
  public static List<Integer> asList(int[] arr) {
    List<Integer> list = Lists.newArrayList();
    for (int i=0; i<arr.length; i++){
      list.add(arr[i]);
    }
    return list;
  }
  
  public static int[] fromList(List<Integer> list){
    int[] retval = new int[list.size()];
    for (int i=0; i<list.size(); i++){
      retval[i] = list.get(i);
    }
    return retval;
  }

	/**
	 * Calculates a dense histogram of the values contained in arr.
	 */
	public static int[] denseCounterOf(int[] arr, int numOfOutcomes) {
		int[] hist = new int[numOfOutcomes];
		for (int val : arr) {
			hist[val] += 1;
		}
		return hist;
	}
	
	public static void multiplyAndRoundToSelf(int[] arr, double value){
		for (int i=0; i<arr.length; i++){
			arr[i] = (int)Math.round(arr[i]*value);
		}
	}

	public static int[] add(int[] arr, int val){
		int[] result = arr.clone();
		addToSelf(result, val);
		return result;
	}
	
	public static void addToSelf(int[] arr, int val){
		for (int i=0; i<arr.length; i++){
			arr[i] += val;
		}
	}
	
}
