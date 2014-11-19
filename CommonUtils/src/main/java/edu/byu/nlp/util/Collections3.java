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

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;

import org.apache.commons.math3.random.RandomAdaptor;
import org.apache.commons.math3.random.RandomGenerator;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import edu.byu.nlp.stats.RandomGenerators;

/**
 * @author rah67
 *
 */
public class Collections3 {

	private Collections3() { }
	
	/**
	 * Only if list:
	 * The semantics of the collection returned by this method become undefined if the backing collection (i.e., this
	 * collection) is structurally modified in any way other than via the returned collection. (Structural modifications
	 * are those that change the size of this collection, or otherwise perturb it in such a fashion that iterations in
	 * progress may yield incorrect results.)
	 */
	public static <E> Collection<E> limit(final Collection<E> coll, final int limitSize) {
		if (coll instanceof List) {
			return ((List<E>) coll).subList(0, limitSize);
		}
		
		return new AbstractCollection<E>() {

			@Override
			public Iterator<E> iterator() {
				return Iterators.limit(coll.iterator(), limitSize);
			}

			@Override
			public int size() {
				return Math.min(limitSize, coll.size());
			}
			
		};
	}
	
	/**
	 * Only if list:
	 * The semantics of the collection returned by this method become undefined if the backing collection (i.e., this
	 * collection) is structurally modified in any way other than via the returned collection. (Structural modifications
	 * are those that change the size of this collection, or otherwise perturb it in such a fashion that iterations in
	 * progress may yield incorrect results.)
	 */
	public static <E> Collection<E> skip(final Collection<E> coll, final int numberToSkip) {
		if (coll instanceof List) {
			return ((List<E>) coll).subList(numberToSkip, coll.size());
		}
		
		Preconditions.checkElementIndex(numberToSkip, coll.size());
		
		return new AbstractCollection<E>() {

			@Override
			public Iterator<E> iterator() {
				Iterator<E> it = coll.iterator();
				Iterators.advance(it, numberToSkip);
				return it;
			}

			@Override
			public int size() {
				return Math.max(0, coll.size() - numberToSkip);
			}
			
		};
	}
	
	/**
	 * Creates a new collection that is logically the concatenation of the provided collections. The returned collection
	 * is a view over the original two.
	 */
	public static <E> Collection<E> concat(final Collection<E> coll1, final Collection<E> coll2) {
		return new AbstractCollection<E>() {

			@Override
			public Iterator<E> iterator() {
				return Iterators.concat(coll1.iterator(), coll2.iterator());
			}

			@Override
			public int size() {
				return coll1.size() + coll2.size();
			}
			
		};
	}
	
	/**
	 * Samples the specified number of elements with replacement from the specified collection with uniform probability.
	 * If coll is a random access list, then sampleWithReplacementInternal(List<T>, int, RandomGenerator) is invoked. 
	 */
	public static <T> List<T> sampleWithReplacement(Collection<T> coll, int numSamples, RandomGenerator rnd) {
		if (coll instanceof RandomAccess)
			return sampleWithReplacement((List<T>)coll, numSamples, rnd);

		if (numSamples == 1)	// Faster for a single sample
			return Collections.singletonList(sample(coll, rnd));
		
		return sampleWithReplacementInternal(coll, numSamples, rnd);
	}

	private static <T> List<T> sampleWithReplacementInternal(Collection<T> coll, int numSamples, RandomGenerator rnd) {
		// NOTE : we might be able to have fewer calls to the rbinom subroutine
		// if we figured out how many we could skip (p(numskips)) 
		
		List<T> samples = new ArrayList<T>(numSamples);
		
		int numLeftInCollection = coll.size();
		int numStillNeeded = numSamples;
		for (Iterator<T> iterator = coll.iterator(); numStillNeeded > 0;) {
			T item = iterator.next();
			
			// The number of times we will pick this item is given by binomial distribution
			// with parameters p = 1/numLeftInCollection and k = numStillNeeded
			int numToTake = RandomGenerators.nextBinom(rnd, numStillNeeded, 1.0 / numLeftInCollection);
			if (numToTake > 0) {
				for(int i = 0; i < numToTake; i++) {
					samples.add(item);
				}
				numStillNeeded -= numToTake;
			}
			--numLeftInCollection;
		}
		
		return samples;
	}

	/**
	 * Samples the specified number of elements with replacement from the specified list with uniform probability. 
	 */
	public static <T> List<T> sampleWithReplacement(List<T> list, int numSamples, RandomGenerator rnd) {
		numSamples = Math.min(list.size(), numSamples);

		if (numSamples == 1)	// Faster for a single sample
			return Collections.singletonList(sample(list, rnd));
		
		if (!(list instanceof RandomAccess))	// Avoids n^2 behavior of linked lists
			return sampleWithReplacementInternal((Collection<T>)list, numSamples, rnd);
		
		List<T> samples = new ArrayList<T>();
		for(int i = 0; i < numSamples; i++) {
			int index = rnd.nextInt(list.size());
			samples.add(list.get(index));
		}
		return samples;
	}
	
	/**
	 * Samples a single element with replacement from the specified collection with uniform probability.
	 */
	public static <T> T sample(Collection<T> coll, RandomGenerator rnd) {
		if (coll instanceof List) {
			// More efficient for Random access lists.
			return sample((List<T>)coll, rnd);
		}
		
		return Iterables.get(coll, rnd.nextInt(coll.size()));
	}

	/**
	 * Samples a single element from the specified list with uniform probability.
	 */
	public static <T> T sample(List<T> coll, RandomGenerator rnd) {
		return coll.get(rnd.nextInt(coll.size()));
	}

	/**
	 * Creates a copy the iterable and uses the provided RandomGenerator to shuffle the copy.
	 */
	public static <E> List<E> shuffledCopyOf(Iterable<? extends E> it, RandomGenerator rnd) {
		List<E> list = Lists.newArrayList(it);
		Collections.shuffle(list, new RandomAdaptor(rnd));
		return list;
	}
	
	/**
	 * Returns a sorted copy of the iterable using the natural ordering.
	 */
	public static <E extends Comparable<? super E>> List<E> sortedCopyOf(Iterable<? extends E> it) {
		ArrayList<E> copy = Lists.newArrayList(it);
		Collections.sort(copy);
		return copy;
	}

	/**
	 * Returns a sorted copy of the iterable using the specified comparator.
	 */
	public static <E> List<E> sortedCopyOf(Iterable<? extends E> it, Comparator<? super E> comparator) {
		ArrayList<E> copy = Lists.newArrayList(it);
		Collections.sort(copy, comparator);
		return copy;
	}
	
	/**
	 * Sorts a list in place and returns the same list. 
	 */
	public static <E extends Comparable<? super E>, L extends List<E>> L sort(L list) {
		Collections.sort(list);
		return list;
	}
	
	/**
	 * Sorts a list in place and returns the same list. 
	 */
	public static <E, L extends List<E>> L sort(L list, Comparator<? super E> comparator) {
		Collections.sort(list, comparator);
		return list;
	}

    public static <E> void shuffle(List<E> arr, RandomGenerator rnd) {
        for (int i = arr.size() - 1; i > 0; i--) {
            Collections.swap(arr, i, rnd.nextInt(i + 1));
        }
    }
}