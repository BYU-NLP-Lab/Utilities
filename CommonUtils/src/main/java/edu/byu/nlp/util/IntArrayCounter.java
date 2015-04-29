/**
 * Copyright 2013 Brigham Young University
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maintain integer counts over an array of positions
 */
public class IntArrayCounter {
  private static final Logger logger = LoggerFactory.getLogger(IntArrayCounter.class);
  
  // assignment counts
  Counter<Integer>[] counts;
  private int numCategories;

  public IntArrayCounter(int size, int numCategories) {
    this.numCategories = numCategories;
    counts = new DenseCounter[size]; 
  }

  public void reset() {
    counts = new DenseCounter[counts.length];
  }
  
  public IntArrayCounter clone(){
	  IntArrayCounter cnt = new IntArrayCounter(counts.length, numCategories);
	  int[][] values = values();
	  for (int i=0; i<values.length; i++){
		  int[] vals = values[i];
		  for (int c=0; c<vals.length; c++){
			  cnt.increment(i, c, values[i][c]);
		  }
	  }
	  return cnt;
  }

//  public void remapLabels(int[] map) {
//    IntArrayCounter translated = new IntArrayCounter(counts.length,numCategories);
//    for (int pos = 0; pos < counts.length; pos++) {
//      for (int srcElement = 0; srcElement < map.length; srcElement++) {
//        translated.increment(pos, map[srcElement], counts[pos].getCount(srcElement));
//      }
//    }
//    this.counts = translated.counts;
//  }

  public int[] argmax() {
    int[] argmax = new int[counts.length];
    for (int i=0; i<counts.length; i++){
      argmax[i] = argmax(i);
    }
    return argmax;
  }

  public int[][] values(){
    int[][] result = new int[counts.length][];
    for (int i=0; i<counts.length; i++){
      result[i] = values(i);
    }
    return result;
  }
  
  public int[] values(int i){
    int[] vals = new int[numCategories];
    for (int e=0; e<numCategories; e++){
      vals[e] = (counts[i]==null)? 0: counts[i].getCount(e);
    }
    return vals;
  }
  
  public int argmax(int i) {
    if (counts[i]==null || counts[i].totalCount()==0){
      logger.warn("Asked for argmax of empty counts. Returning -1.");
      return -1;
    }
    return counts[i].argMax();
  }

  public void increment(int[] vals) {
    for (int i=0; i<vals.length; i++){
      increment(i, vals[i]);
    }
  }
  
  public void increment(int i, int val) {
    increment(i, val, 1);
  }

  public void increment(int i, int val, int count) {
    if (counts[i] == null) {
      counts[i] = new DenseCounter(numCategories+1);
    }
    counts[i].incrementCount(val, count);
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return Arrays.toString(counts);
  }
}