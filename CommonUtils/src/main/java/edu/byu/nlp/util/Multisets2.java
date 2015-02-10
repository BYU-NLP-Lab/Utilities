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

import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;

import com.google.common.collect.Multiset;

import edu.byu.nlp.stats.RandomGenerators;

/**
 * @author pfelt
 *
 */
public class Multisets2 {

  /**
   * Return the element(s) with the largest count in a multiset, or else null
   * if there are no elements
   */
  public static <T> Set<T> maxElements(Multiset<T> mset){
    return getArgMinMax(mset).argmax();
  }

  /**
   * Choose an arbitrary max element
   */
  public static <T> T  maxElement(Multiset<T> mset, RandomGenerator rnd){
	  Set<T> maxElements = maxElements(mset);
	  return maxElements.size()==0? null: RandomGenerators.sample(maxElements, rnd);
  }

  /**
   * @return the count of the element that appeared 
   * the largest number of times
   */
  public static <T> int maxCount(Multiset<T> mset){
    return getArgMinMax(mset).max();
  }
	  
  /**
   * Return the element(s) with the smallest count in a multiset, or else null
   * if there are no elements
   */
  public static <T> Set<T> minElements(Multiset<T> mset){
    return getArgMinMax(mset).argmin();
  }

  /**
   * Choose an arbitrary min element
   */
  public static <T> T  minElement(Multiset<T> mset, RandomGenerator rnd){
	  Set<T> minElements = minElements(mset);
	  return minElements.size()==0? null: RandomGenerators.sample(minElements, rnd);
  }
  
  /**
   * @return the count of the element that appeared 
   * the fewest number of times
   */
  public static <T> int minCount(Multiset<T> mset){
    return getArgMinMax(mset).min();
  }
  
  public static <T> ArgMinMaxTracker<Integer, T> getArgMinMax(Multiset<T> mset){
	ArgMinMaxTracker<Integer,T> tracker = ArgMinMaxTracker.newArgMinMaxTracker();
    for (com.google.common.collect.Multiset.Entry<T> entry: mset.entrySet()){
    	tracker.offer(entry.getCount(),entry.getElement());
    }
    return tracker;
  }
}
