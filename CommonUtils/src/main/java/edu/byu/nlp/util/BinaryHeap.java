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

import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Deque;
import java.util.List;
import java.util.NoSuchElementException;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;

/**
 * A Binary min-heap implementation with an iterator that allows for promotion/demotion via the replace() method.
 * 
 * NOTE: Iterator is not currently fail-fast.
 * 
 * @author rah67
 *
 */
public class BinaryHeap<V> extends AbstractHeap<V> {

	private final List<V> values;
	private final Comparator<? super V> comparator;
	private int modCount;
	
	public BinaryHeap(Comparator<? super V> comparator) {
		this(Lists.<V>newArrayList(), comparator);
	}
	
	BinaryHeap(List<V> values, Comparator<? super V> comparator) {
		this.values = values;
		this.comparator = comparator;
		this.modCount = 0;
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean isEmpty() {
		return values.isEmpty();
	}
	
	/** {@inheritDoc} */
	@Override
	public int size() {
		return values.size();
	}
	
	private int parentIndex(int index) {
		assert(index >= 0 && index < size());
		return (index - 1) >> 1;
	}
	
	private int leftChildIndex(int index) {
		assert(index >= 0 && index < size());
		return (index << 1) + 1;
	}
	
	private int rightChildIndex(int index) {
		return leftChildIndex(index) + 1;
	}
	
	private void addLast(V val) {
		values.add(val);
	}
	
	private V removeLast() {
		return values.remove(values.size() - 1);
	}
	
	private V get(int index) {
		assert(index >= 0 && index < size());
		return values.get(index);
	}
	
	private void set(int index, V val) {
		assert(index >= 0 && index < size());
		values.set(index, val);
	}
	
	private int childIndexForDownHeap(int index) {
		assert(index >= 0 && index < size());
		int leftChild = leftChildIndex(index);
		if (leftChild >= size()) {
			return leftChild;
		}
		int rightChild = rightChildIndex(index);
		if (rightChild >= size() || comparator.compare(get(leftChild), get(rightChild)) <= 0) {
			return leftChild;
		}
		return rightChild;
	}
	
	private void swap(int index1, int index2) {
		V tmp = get(index1);
		set(index1, get(index2));
		set(index2, tmp);
	}
	
	// TODO(rah67): reduce redundant calls to get
	private int upHeap(int index) {
		int parent = parentIndex(index);
		while (parent >= 0 && comparator.compare(get(index), get(parent)) < 0) {
			swap(parent, index);
			index = parent;
			parent = parentIndex(index);
		}
		return index;
	}
	
	// TODO(rah67): reduce redundant calls to get
	private int downHeap(int index) {
		int minChild = childIndexForDownHeap(index);
		while (minChild < size() && comparator.compare(get(index), get(minChild)) > 0) {
			swap(minChild, index);
			index = minChild;
			minChild = childIndexForDownHeap(index);
		}
		return index;
	}
	
	/**
	 * Removes the element at the specified index while maintaining the heap invariant. Note that the return value is
	 * NOT the value removed. When the last element in the underlying list is not a descendant of the parent of the
	 * item at the index being removed, then we may have to perform an upHeap() operation. The result is that an element
	 * after i ends up before i in the underlying list; the iterator needs to know of this situation to properly iterate
	 * over all elements.
	 */
	private V remove(int index) {
		assert(index >= 0 && index < size());
		++modCount;
		// Maintain the heap invariant.
		// First, maintain the shape property via a swap with the last index.
		V prevLastVal = removeLast();
		if (index == size()) {
			// Removing the last element requires no further work.
			return null;
		}
		
		set(index, prevLastVal);
		int newIndex = downHeap(index);
		if (newIndex == index) {
			// Either the item needs to go up, or stay
			newIndex = upHeap(index);
			if (newIndex != index) {
				assert newIndex < index;
				return prevLastVal;
			}
		}
		return null;
	}
	
	/**
	 * Updates the value of the element at the specified index while maintaining the heap invariant. Note that, like
	 * removeAt(int), the return value is used by the iterator and is NOT the element at the specified index.
	 */
	private boolean replace(int index, V val) {
		assert(index >= 0 && index < size());
		++modCount;
		set(index, val);
		int newIndex = upHeap(index);
		if (newIndex == index) {
			// Didn't move, so maybe it should go down
			newIndex = downHeap(index);
			if (newIndex != index) {
				// This time, it moved down.
				assert(newIndex > index);
				return true;
			}
		}
		return false;
	}
	
	@Override
	protected boolean offerNotNull(V val) {
		int index = size();
		// Maintain the heap invariant
		addLast(val);
		upHeap(index);
		++modCount;
		// TODO(rah67): make overflow aware
		return true;
	}
	
	/** {@inheritDoc} */
	@Override
	public V poll() {
		if (isEmpty()) {
			throw new NoSuchElementException(); 
		}
		
		V minVal = get(0);

		// Maintain the heap invariant
		// First, maintain the shape property via a swap with the last index unless this is the only element.
		V lastVal = removeLast();
		if (!values.isEmpty()) {
			set(0, lastVal);
			downHeap(0);
		}
		++modCount;
		return minVal;
	}
	
	/** {@inheritDoc} */
	@Override
	public V peek() {
		if (isEmpty()) {
			return null;
		}
		return values.get(0);
	}
	
	/** {@inheritDoc} */
	@Override
	public V offerThenPoll(V val) {
		if (isEmpty()) {
			return val;
		}
		
		V minVal = get(0);
		if (comparator.compare(val, minVal) <= 0) {
			// The argument IS the least element
			return val;
		}
		
		// Maintain the heap invariant
		set(0, val);
		downHeap(0);
		++modCount;
		return minVal;
	}
	
	@VisibleForTesting void heapify() {
		if (isEmpty()) {
			return;
		}
		
		// downHeap does nothing for leaf nodes; due to the shape property of a heap, the first non-leaf
		// is the parent of the last element.
		for (int i = parentIndex(size() - 1); i >= 0; i--) {
			downHeap(i);
		}
		++modCount;
	}
	
	// In order to avoid creating a large number of unnecessary objects, the design precludes the ability to 
	// have both a curState and nextState at the same time.
	private static interface IteratorState<V> {
		boolean hasNext();
		IteratorState<V> nextState();
		V getValue();
		void remove();
		void replace(V val);
	}
	
	private final class InitialState implements IteratorState<V> {

		@Override public boolean hasNext() { return !isEmpty(); }
		
		@Override
		public IteratorState<V> nextState() {
			if (!hasNext()) {
				return new DoneState<V>();
			}
			return new ListState();
		}

		@Override public V getValue() { throw new IllegalStateException(); }
		@Override public void remove() { throw new IllegalStateException(); }
		@Override public void replace(V val) { throw new IllegalStateException(); }
	}
	
	private final class ListState implements IteratorState<V> {

		private Deque<V> skipped = Queues.<V>newArrayDeque();
		private Counter<V> toSkip = new IdentityCounter<V>();
		private int i = 0;

		@Override
		public boolean hasNext() {
			int j = i + 1;
			while ((j < size()) && (toSkip.getCount(get(j)) > 0)) {
				++j;
			}
			return (j < size() || !skipped.isEmpty());
		}
		
		@Override
		public IteratorState<V> nextState() {
			do {
				++i;
			} while (i < size() && toSkip.getCount(get(i)) > 0);
			if (i < size()) {
				return this;
			} else if (!skipped.isEmpty()) {
				return new SkippedState(skipped);
			}
			assert(toSkip.numEntries() == 0);
			return new DoneState<V>();
		}
		
		@Override
		public V getValue() {
			return get(i);
		}
		
		@Override
		public void remove() {
			V skippedValue = BinaryHeap.this.remove(i);
			if (skippedValue != null) {
				// The last item on the heap was upHeaped and therefore will not be seen. 
				if (toSkip.getCount(skippedValue) > 0) {
					// TODO(rah67): add test case for this
					toSkip.decrementCount(skippedValue, 1);
				} else {
					skipped.offerLast(skippedValue);
				}
			} else {
				// The items below us have shifted up, so we need to revisit this location.
				--i;
			}
		}
		
		@Override
		public void replace(V val) {
			if (BinaryHeap.this.replace(i, val)) {
				toSkip.incrementCount(val, 1);
				// Take care to not skip the item that moved into this place.
				skipped.offerLast(get(i));
			}
		}
	}
	
	private final class SkippedState implements IteratorState<V> {
		
		private Deque<V> skipped;
		private V curVal;
		private boolean removed;
		
		public SkippedState(Deque<V> skipped) {
			assert(!skipped.isEmpty());
			this.skipped = skipped;
			this.curVal = skipped.pollLast();
			this.removed = false;
		}
		
		@Override
		public boolean hasNext() {
			return !skipped.isEmpty();
		}

		@Override
		public IteratorState<V> nextState() {
			if (!hasNext()) {
				return new DoneState<V>();
			}
			curVal = skipped.pollLast();
			removed = false;
			return this;
		}
		
		@Override
		public V getValue() {
			return curVal;
		}
		
		@Override
		public void remove() {
			if (removed) {
				// TODO(rah67): add test case
				return;
			}
			
			V skippedValue = BinaryHeap.this.remove(findLastReturnedSkip());
			assert(skippedValue == null);
			removed = true;
		}
		
		// The only way to guarantee we find the item is to do a linear search since subsequent removals/updates
		// may have moved the item.
		private int findLastReturnedSkip() {
			int index = 0;
			while (get(index) != curVal) {
				++index;
			}
			return index;
		}

		@Override
		public void replace(V val) {
			if (removed) {
				// TODO(rah67): add test case
				throw new NoSuchElementException();
			}
			
			// TODO(rah67): test this functionality!
			boolean skip = BinaryHeap.this.replace(findLastReturnedSkip(), val);
			assert(!skip);
		}
	}
	
	private static final class DoneState<V> implements IteratorState<V> {
		@Override public boolean hasNext() { throw new IllegalStateException(); }
		@Override public IteratorState<V> nextState() { throw new IllegalStateException(); }
		@Override public V getValue() { throw new IllegalStateException(); }
		@Override public void remove() { throw new IllegalStateException(); }
		@Override public void replace(V val) { throw new IllegalStateException(); }
	}
	
	private class HeapIteratorImpl implements HeapIterator<V> {
		
		private IteratorState<V> state;
		private int expectedModCount = modCount;
		
		/*
		 * It is not legal for remove to be invoked more than once before subsequent invocations to next(). It also is
		 * not legal to invoke replace() after an invocation of remove() but before invoking next().
		 */
		private boolean removeInvoked = true;
		private boolean replaceInvoked = false;
		
		private HeapIteratorImpl(IteratorState<V> initialState) {
			this.state = initialState;
		}
		
		@Override
		public boolean hasNext() {
			return state.hasNext();
		}

		@Override
		public V next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			if (expectedModCount != modCount) {
				throw new ConcurrentModificationException();
			}
			removeInvoked = false;
			replaceInvoked = false;
			state = state.nextState();
			return state.getValue();
		}

		@Override
		public void remove() {
			if (expectedModCount != modCount) {
				throw new ConcurrentModificationException();
			}
			if (removeInvoked) {
				throw new IllegalStateException();
			}
			state.remove();
			expectedModCount = modCount;
			removeInvoked = true;
		}

		@Override
		public void replace(V val) {
			if (expectedModCount != modCount) {
				throw new ConcurrentModificationException();
			}
			if (removeInvoked || replaceInvoked) {
				throw new IllegalStateException();
			}
			state.replace(val);
			expectedModCount = modCount;
			replaceInvoked = true;
		}
	}
	
	/** {@inheritDoc} */
	@Override
	public HeapIterator<V> iterator() {
		return new HeapIteratorImpl(new InitialState());
	}
}