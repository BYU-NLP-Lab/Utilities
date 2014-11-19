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
	 * The V type MUST be a subclass of Number. I can't figure out the generics to make that work (pfelt).
	 */
  public static <E,V> List<E> argMaxList(Set<Entry<E, V>> entrySet, int topn, RandomGenerator rnd) {
    topn = (topn>0)? topn: entrySet.size();
    
    List<Entry<E, V>> entries = Lists.newArrayList(entrySet);
    if (rnd!=null){
      Collections.shuffle(entries, new Random(rnd.nextLong()));
    }
    Collections.sort(entries,new Comparator<Entry<E, V>>() {
      @Override
      public int compare(Entry<E, V> o1, Entry<E, V> o2) {
        double c1 = (o1==null)? 0: ((Number)o1.getValue()).doubleValue();
        double c2 = (o2==null)? 0: ((Number)o2.getValue()).doubleValue();
        return Double.compare(c2, c1); // descending order
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