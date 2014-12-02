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

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;

import com.google.common.collect.Lists;

/**
 * @author rah67
 * @author plf1
 *
 */
public class Counters {

	private Counters() { }

	public static <E> Counter<E> count(Iterable<E> it) {
		return count(it.iterator());
	}
	
	public static <E> Counter<E> count(Iterator<E> it) {
		Counter<E> counter = new HashCounter<E>();
		while (it.hasNext()) {
			counter.incrementCount(it.next(), 1);
		}
		return counter;
	}
	
	public static <E> Counter<E> identityCount(Iterable<E> it) {
		return identityCount(it.iterator());
	}
	
	public static <E> Counter<E> identityCount(Iterator<E> it) {
		Counter<E> counter = new IdentityCounter<E>();
		while (it.hasNext()) {
			counter.incrementCount(it.next(), 1);
		}
		return counter;
	}
	
	/**
	 * Get the n entries with the largest value based on some comparator. 
	 * Used by Counter's argMaxList method. 
	 */
  public static <E,V extends Comparable<V>> List<E> argMaxList(Set<Entry<E, V>> entrySet, int topn, RandomGenerator rnd) {
    topn = (topn>0)? topn: entrySet.size();
    
    
    // TODO: The sorting hacks below were intended to simply ensure that 
    // we matched previous implementations, but when they are removed 
    // item/mom resp performance tanks. Something is going on! 
    // (probably related to some indexer?)
    List<Entry<E, V>> entries = Lists.newArrayList(entrySet);
    Collections.sort(entries, new Comparator<Entry<E, V>>() {
		@Override
		public int compare(Entry<E, V> o1, Entry<E, V> o2) {
			return ((Comparable<E>)o1.getKey()).compareTo(o2.getKey());
		}
	});
    // FIXME: undo this and shuffle again
//    if (rnd!=null){
//      Collections.shuffle(entries, new Random(rnd.nextLong()));
//    }
    Collections.sort(entries,new Comparator<Entry<E, V>>() {
	@Override
      public int compare(Entry<E, V> o1, Entry<E, V> o2) {
    	  return (o2.getValue()).compareTo(o1.getValue()); // descending order
      }
    });
    // pull out the top n values
    List<E> vals = Lists.newArrayList();
    for (int i=0; i<Math.min(topn, entries.size()); i++){
      vals.add(entries.get(i).getKey());
    }
    return vals;
  }
}