/**
 * Copyright 2014 Brigham Young University
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

import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import com.google.common.collect.Multiset;

import edu.byu.nlp.util.ArgMinMaxTracker.MinMaxTracker;

/**
 * @author pfelt
 *
 */
public class Multisets2 {

  /**
   * Return the element(s) with the largest count in a multiset, or else null
   * if there are no elements (highest to lowest)
   */
  public static <T> List<T> maxElements(Multiset<T> mset, int topk, RandomGenerator rnd){
    return getArgMinMax(rnd,topk,mset).argmax();
  }

  /**
   * Choose max element (arbitrary breaking ties)
   */
  public static <T> T maxElement(Multiset<T> mset, RandomGenerator rnd){
    return getArgMinMax(rnd,1,mset).argmin().get(0);
  }

  /**
   * @return the count of the element that appeared 
   * the largest number of times
   */
  public static <T> int maxCount(Multiset<T> mset){
    return getMinMax(mset).max().get(0);
  }

  /**
   * Choose min k elements (arbitrary breaking ties). Ordered low to high.
   */
  public static <T> T  minElements(Multiset<T> mset, int topk, RandomGenerator rnd){
    return getArgMinMax(rnd,topk,mset).argmin().get(0);
  }
  
  /**
   * Choose an arbitrary min element
   */
  public static <T> T  minElement(Multiset<T> mset, RandomGenerator rnd){
    return getArgMinMax(rnd,1,mset).argmin().get(0);
  }
  
  /**
   * @return the count of the element that appeared 
   * the fewest number of times
   */
  public static <T> int minCount(Multiset<T> mset){
    return getMinMax(mset).min().get(0);
  }

  public static <T> MinMaxTracker<Integer> getMinMax(Multiset<T> mset){
    MinMaxTracker<Integer> tracker = MinMaxTracker.create();
    for (com.google.common.collect.Multiset.Entry<T> entry: mset.entrySet()){
      tracker.offer(entry.getCount(),entry.getElement());
    }
    return tracker;
  }
  
  public static <T> ArgMinMaxTracker<Integer, T> getArgMinMax(RandomGenerator rnd, int topk, Multiset<T> mset){
	ArgMinMaxTracker<Integer,T> tracker = ArgMinMaxTracker.create(rnd,topk);
    for (com.google.common.collect.Multiset.Entry<T> entry: mset.entrySet()){
    	tracker.offer(entry.getCount(),entry.getElement());
    }
    return tracker;
  }
}
