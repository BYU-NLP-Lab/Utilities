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

import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import com.google.common.collect.Ordering;

/**
 * @author rah67, pfelt
 *
 */
public class Iterators2 {

	private Iterators2() { }

	public static <E> List<E> topN(Iterator<? extends E> it, int n, Comparator<? super E> c) {
		return Heaps.largestN(it, n, true, c);
	}
	
	public static <E extends Comparable<? super E>> E max(Iterator<? extends E> it) {
		return max(it, Ordering.<E>natural());
	}
	
	public static <E extends Comparable<? super E>> E min(Iterator<? extends E> it) {
		return max(it, Collections.reverseOrder());
	}
	
	public static <E> E max(Iterator<? extends E> it, Comparator<? super E> comparator) {
		if (!it.hasNext()) { 
			return null;
		}
		
		E max = it.next();
		while (it.hasNext()) {
			E next = it.next();
			if (comparator.compare(next, max) > 0) {
				max = next;
			}
		}
		return max;
	}
	
	public static <E> Iterator<Enumeration<E>> enumerate(final Iterator<E> iterator) {
		return new Iterator<Enumeration<E>>() {

			private int i = 0;
			
			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public Enumeration<E> next() {
				return new Enumeration<E>(i++, iterator.next());
			}

			@Override
			public void remove() {
				iterator.remove();
			}
			
		};
	}
	
	private static class RepeatItemIterator<E> implements Iterator<E> {

	    private final Iterator<E> it;
	    private final int n;
	    private E next;
	    private int curCount;
	    
        public RepeatItemIterator(Iterator<E> it, int n) {
            this.it = it;
            this.n = n;
            this.curCount = n;  // allows the hasNext() condition to work correctly for empty it
            
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasNext() {
            return it.hasNext() || curCount < n;
        }

        /** {@inheritDoc} */
        @Override
        public E next() {
            if (curCount >= n) {
                assert curCount == n;
                next = it.next();
                curCount = 0;
            }
            ++curCount;
            return next;
        }

        /** {@inheritDoc} */
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
	    
	}
	
	/**
	 * Repeats each element of the original iterator n times in order. Currently, does not support removal.
	 */
	public static <E> Iterator<E> repeatItems(Iterator<E> it, int n) {
	    return new RepeatItemIterator<E>(it, n);
	}

	private static class CycleProvidingIterator<E> implements Iterator<Iterator<E>> {

	    private final Iterable<E> it;
	    private final int n;
	    private int curCount = 0;
	    
	    public CycleProvidingIterator(Iterable<E> it, int n) {
	        this.it = it;
	        this.n = n;
	    }
	    
        /** {@inheritDoc} */
        @Override
        public boolean hasNext() {
            return curCount < n;
        }

        /** {@inheritDoc} */
        @Override
        public Iterator<E> next() {
            Preconditions.checkState(curCount < n);
            ++curCount;
            return it.iterator();
        }

        /** {@inheritDoc} */
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
	}
	
    /**
	 * Cycles the instances of it exactly n times in the order of iteration.
	 */
	public static <E> Iterator<E> cycle(Iterable<E> it, int n) {
	    return Iterators.concat(new CycleProvidingIterator<E>(it, n));
	}
	

    /**
     * Iterates one after another through all iterators in an iterable. 
     */
	public static <T> Iterator<T> lazyConcatenate(Iterable<Iterator<T>> iterators){
		final Iterator<Iterator<T>> outerItr = iterators.iterator();
		
		return new AbstractIterator<T>() {
			Iterator<T> innerItr = null;
			
			@Override
			protected T computeNext() {
				// find a valid inner iterator
				while (innerItr==null || !innerItr.hasNext()){
					if (outerItr==null || !outerItr.hasNext()){
						return endOfData();
					}
					innerItr = outerItr.next();
				}
				return innerItr.next();
			}
		};
	}
}
