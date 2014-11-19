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

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;


/**
 * @author rah67, pfelt
 *
 */
public class Iterables2 {
	
	public static <E> List<E> topN(final Iterable<? extends E> it, final Comparator<? super E> c, final int n) {
		return Iterators2.topN(it.iterator(), n, c);
	}

	public static <E> Iterable<Enumeration<E>> enumerate(final Iterable<E> iterable) {
		return new Iterable<Enumeration<E>>() {

			@Override
			public Iterator<Enumeration<E>> iterator() {
				return Iterators2.enumerate(iterable.iterator());
			}
			
		};
	}
	
	public static <E extends Comparable<? super E>> E max(final Iterable<? extends E> it) {
		return Iterators2.max(it.iterator());
	}

    /**
     * @param labeled
     * @param unlabeled
     * @return
     */
    public static <F, S> Iterable<Pair<F, S>> pairUp(final Iterable<? extends F> first,
            final Iterable<? extends S> second) {
        return new Iterable<Pair<F, S>>() {
            @Override
            public Iterator<Pair<F, S>> iterator() {
                return new Iterator<Pair<F, S>>() {
                    
                    private final Iterator<? extends F> firstIt = first.iterator();
                    private final Iterator<? extends S> secondIt = second.iterator();
                    
                    @Override
                    public boolean hasNext() {
                        return firstIt.hasNext() && secondIt.hasNext();
                    }

                    @Override
                    public Pair<F, S> next() {
                        return Pair.<F, S>of(firstIt.next(), secondIt.next());
                    }

                    @Override
                    public void remove() {
                        firstIt.remove();
                        secondIt.remove();
                    }
                };
            }
        };
    }

    /**
     * Provides the same functionality as guava's Iterables.partition() but without a couple of 
     * shortcomings: handles split size 0, and the second collection can be bigger or smaller 
     * than the first.
     */
    @SuppressWarnings("unchecked")
	public static <I> Iterable<? extends Collection<I>> partition(
        Collection<I> labeledData, int splitSize) {
      Iterator<I> itr = labeledData.iterator();
      Collection<I> first = Lists.newArrayList();
      for (int cnt=0; itr.hasNext() && cnt<splitSize; cnt++){
        first.add(itr.next());
      }
      Collection<I> second = Lists.newArrayList();
      while (itr.hasNext()){
        second.add(itr.next());
      }
      return Lists.newArrayList(first,second);
    }
    

	
    
	
}
