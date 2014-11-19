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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

/**
 * @author rah67
 *
 */
public class Heaps {

	private Heaps() { }
	
	public static <V extends Comparable<? super V>> Heap<V> newBinaryMaxHeap() {
		return new BinaryHeap<V>(Collections.reverseOrder());
	}
	
	public static <V> Heap<V> newBinaryMaxHeap(Comparator<? super V> c) {
		return new BinaryHeap<V>(Collections.reverseOrder(c));
	}
	
	public static <V extends Comparable<? super V>> Heap<V> newBinaryMaxHeapFromValues(V... values) {
		return newBinaryMinHeapFrom(Arrays.asList(values));
	}
	
	public static <V extends Comparable<? super V>> Heap<V> newBinaryMaxHeapFrom(Iterable<? extends V> it) {
		List<V> values = Lists.newArrayList(it);			// shallow copy
		BinaryHeap<V> heap = new BinaryHeap<V>(values, Collections.<V>reverseOrder());
		heap.heapify();
		return heap;
	}

	public static <V extends Comparable<? super V>> Heap<V> newBinaryMinHeap() {
		return new BinaryHeap<V>(Ordering.<V>natural());
	}
	
	public static <V> Heap<V> newBinaryMinHeap(Comparator<? super V> c) {
		return new BinaryHeap<V>(c);
	}
	
	public static <V extends Comparable<? super V>> Heap<V> newBinaryMinHeapFromValues(V... values) {
		return newBinaryHeapFrom(Arrays.asList(values), Ordering.<V>natural());
	}
	
	public static <V extends Comparable<? super V>> Heap<V> newBinaryMinHeapFrom(Iterable<? extends V> it) {
		return newBinaryHeapFrom(it, Ordering.<V>natural());
	}
	
	public static <V> Heap<V> newBinaryHeapFrom(Iterable<? extends V> it, Comparator<V> c) {
		return newBinaryHeapFrom(it.iterator(), c);
	}

	public static <V> Heap<V> newBinaryHeapFrom(Iterator<? extends V> it, Comparator<V> c) {
		List<V> values = Lists.newArrayList(it);			// shallow copy
		BinaryHeap<V> heap = new BinaryHeap<V>(values, c);
		heap.heapify();
		return heap;
	}
	
	public static <V extends Comparable<? super V>> List<V> largestN(Iterable<? extends V> iterable, int n,
			boolean sort) {
		return largestN(iterable.iterator(), n, sort, Ordering.<V>natural());
	}
	
	public static <V> List<V> largestN(Iterable<? extends V> iterable, int n, boolean sort,
			Comparator<? super V> comparator) {
		return largestN(iterable.iterator(), n, sort, comparator);
	}
	
	public static <V extends Comparable<? super V>> List<V> smallestN(Iterable<? extends V> iterable, int n,
			boolean sort) {
		return largestN(iterable.iterator(), n, sort, Collections.reverseOrder());
	}
	
	public static <V> List<V> smallestN(Iterable<? extends V> iterable, int n, boolean sort,
			Comparator<? super V> comparator) {
		return largestN(iterable.iterator(), n, sort, Collections.reverseOrder(comparator));
	}
	
	public static <V> List<V> largestN(Iterator<? extends V> iterator, int n, boolean sort,
			Comparator<? super V> comparator) {
		Preconditions.checkNotNull(iterator);
		Preconditions.checkArgument(n >= 0);
		Preconditions.checkNotNull(comparator);
		
		if (n == 0) {
			return Collections.emptyList();
		}
		if (n == 1) {
			return Collections.singletonList(Iterators2.max(iterator, comparator));
		}

		// Heapify the first n
		List<V> values = Lists.newArrayListWithCapacity(n);
		Iterators.addAll(values, Iterators.limit(iterator, n));
		BinaryHeap<V> heap = new BinaryHeap<V>(values, comparator);
		heap.heapify();
		
		while (iterator.hasNext()) {
			heap.offerThenPoll(iterator.next());
		}
		if (sort) {
			Collections.sort(values, comparator);
		}
		return values;
	}
	
	public static <V> List<V> drain(Heap<V> h) {
		List<V> coll = Lists.newArrayListWithCapacity(h.size());
		h.drainTo(coll);
		return coll;
	}
	
	public static <V, C extends Collection<V>> C drain(Heap<V> h, Supplier<C> supplier) {
		C coll = supplier.get();
		h.drainTo(coll);
		return coll;
	}
	
	private static class SynchronizedHeap<V> implements Heap<V> {

		private final Heap<V> heap;
		
		public SynchronizedHeap(Heap<V> heap) {
			this.heap = heap;
		}
		
		@Override
		public synchronized boolean offer(V val) {
			return heap.offer(val);
		}

		/** {@inheritDoc} */
		@Override
		public synchronized V poll() {
			return heap.poll();
		}

		/** {@inheritDoc} */
		@Override
		public synchronized V peek() {
			return heap.peek();
		}

		/** {@inheritDoc} */
		@Override
		public synchronized V offerThenPoll(V val) {
			return heap.offerThenPoll(val);
		}

		/** {@inheritDoc} */
		@Override
		public synchronized void merge(Heap<? extends V> other) {
			heap.merge(other);
		}

		/** {@inheritDoc} */
		@Override
		public synchronized boolean isEmpty() {
			return heap.isEmpty();
		}

		/** {@inheritDoc} */
		@Override
		public synchronized int size() {
			return heap.size();
		}

		/** {@inheritDoc} */
		@Override
		public HeapIterator<V> iterator() {
			return heap.iterator();
		}

		/** {@inheritDoc} */
		@Override
		public synchronized boolean remove(Object o) {
			return heap.remove(o);
		}

		/** {@inheritDoc} */
		@Override
		public synchronized void drainTo(Collection<? super V> coll) {
			heap.drainTo(coll);
		}
		
		/** {@inheritDoc} */
		@Override
		public void drainTo(Collection<? super V> coll, int maxElements) {
			heap.drainTo(coll, maxElements);
		}

		/** {@inheritDoc} */
		@Override
		public synchronized boolean equals(Object obj) {
			return heap.equals(obj);
		}

		/** {@inheritDoc} */
		@Override
		public synchronized int hashCode() {
			return heap.hashCode();
		}

		/** {@inheritDoc} */
		@Override
		public synchronized String toString() {
			return heap.toString();
		}

		/** {@inheritDoc} */
		@Override
		public synchronized boolean replace(V oldVal, V newVal) {
			return heap.replace(oldVal, newVal);
		}
	}
	
	/**
	 * Returns a synchronized (thread-safe) heap backed by the specified heap. In order to guarantee
	 * serial access, it is critical that <strong>all</strong> access to the backing collection is accomplished through
	 * the returned collection.<p>
	 * 
	 * It is imperative that the user manually synchronize on the returned collection when iterating over it:
	 * <pre>
	 *  Heap h = Heaps.synchronizedHeap(myHeap);
     *     ...
	 *  synchronized(h) {
	 *      Iterator i = h.iterator(); // Must be in the synchronized block
	 *      while (i.hasNext())
	 *         foo(i.next());
	 *  }
	 * </pre>
	 * Failure to follow this advice may result in non-deterministic behavior.
	 */
	public static <V> Heap<V> synchronizedHeap(Heap<V> heap) {
		return new SynchronizedHeap<V>(heap);
	}

}
