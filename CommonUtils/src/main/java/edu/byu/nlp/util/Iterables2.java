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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.random.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;


/**
 * @author rah67, pfelt
 *
 */
public class Iterables2 {
  private static final Logger logger = LoggerFactory.getLogger(Iterables2.class);
	
	public static <E> Iterator<? extends E> simpleIterator(final Iterable<? extends E> it){
		return Lists.newArrayList(it).iterator();
	}
	
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
    


	public static <E> Iterable<E> subInterval(final Iterable<E> it, final int start, final int length) {
		return Lists.newArrayList(Iterators2.subInterval(it.iterator(), start, length));
	}
	
	public static <E> Iterable<E> firstN(final Iterable<E> iterable, final int n) {
		return subInterval(iterable, 0, n);
	}

	public static <E> Iterable<E> shuffled(final Iterable<E> iterable, RandomGenerator rnd) {
		ArrayList<E> retval = Lists.newArrayList(iterable);
		Collections.shuffle(retval, new Random(rnd.nextLong()));
		return retval;
	}
	
	public static <E> Iterable<E> flatten(final Iterable<? extends Iterable<E>> outerIt){
	  Iterable<E> retval = Lists.newArrayList();
		for (Iterable<E> innerIt: outerIt){
		  retval = Iterables.concat(retval,innerIt);
		}
		return retval;
	}
	
	/**
	 * Similar to guava's Iterables.transform(), but instead of transforming the elements of an 
	 * iterable, this transforms the elements of an Iterable<Iterable> 
	 */
	public static <E,F> Iterable<Iterable<F>> transformIterables(Iterable<Iterable<E>> fromIterable, final Function<? super E,? extends F> function){
	  if (fromIterable==null || function==null){
	    logger.warn("A null value was passed to transformIterables(). Return null.");
	    return null;
	  }
	  return Iterables.transform(fromIterable, new Function<Iterable<E>, Iterable<F>>() {
      @Override
      public Iterable<F> apply(Iterable<E> input) {
        return Iterables.transform(input, function);
      }
    });
	}

  public static <E> Iterable<Iterable<E>> filterNullValuesFromIterables(Iterable<Iterable<E>> fromIterable){
    if (fromIterable==null){
      logger.warn("A null value was passed to transformIterables(). Return null.");
      return null;
    }
    return Iterables.transform(fromIterable, new Function<Iterable<E>, Iterable<E>>() {
      @Override
      public Iterable<E> apply(Iterable<E> input) {
        return Iterables2.filterNullValues(input);
      }
    });
  }
	
	public static <E> Iterable<E> filterNullValues(Iterable<E> iterable){
	  return Iterables.filter(iterable, new Predicate<E>() {
      @Override
      public boolean apply(E input) {
        return input!=null;
      }
    });
	}
}
