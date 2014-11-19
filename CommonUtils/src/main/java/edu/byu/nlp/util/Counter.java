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

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;

/**
 * @author rah67
 *
 * @param <E>
 */
public interface Counter<E> {

	int numEntries();
	
	int decrementCount(E ele, int val);
	
    int incrementCount(E ele, int val);
	
	int getCount(E ele);
	
	int totalCount();
	
	// Ties are broken arbitrarily
	E argMax();
	
	// Ties are broken at random
	E argMax(RandomGenerator rnd);

  /*
   * Ties are broken at random
   * returns a list of objects sorted by descending count value 
   */
  List<E> argMaxList(int topn, RandomGenerator rnd);

	Set<Entry<E, Integer>> entrySet();

}