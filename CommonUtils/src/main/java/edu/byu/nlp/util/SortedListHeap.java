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
import java.util.ConcurrentModificationException;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import com.google.common.collect.Ordering;

/**
 * A heap implementation backed by a linked list that is always sorted. This class is not thread-safe.
 * 
 * @author rah67
 *
 */
public class SortedListHeap<V> extends AbstractHeap<V> implements Heap<V> {

	/**
	 * This dummy node allows us to avoid special-case code for operations on head and tail. The dummy node is ALWAYS
	 * head AND tail of the list. dummyNode.next points to the first REAL node if there is one or to itself if
	 * the list is empty. dummyNode.previous points to the last REAL node, if there is one; otherwise, it points to
	 * itself.
	 */
	private final Node<V> dummyNode;
	private int size;
	private final Comparator<? super V> comparator;
	private int modCount = 0;
	
	/**
	 * Constructs a SortedListHeap initialized to the values contained in the specified Iterable.
	 */
	public static <V extends Comparable<? super V>> SortedListHeap<V> from(Iterable<V> it) {
		Comparator<? super V> comparator = Ordering.<V>natural();
		Node<V> dummy = Node.newDummyNode();
		List<V> sorted = Collections3.sortedCopyOf(it, comparator);
		for (V val : sorted) {
			// Given the way dummy is defined, this adds to the end of the list.
			dummy.createNodeAndInsertBefore(val);
		}
		
		return new SortedListHeap<V>(dummy, sorted.size(), comparator);
	}
	
	/**
	 * Constructs an empty SortedListHeap which will use the specified comparator. 
	 */
	public SortedListHeap(Comparator<? super V> comparator) {
		this(Node.<V>newDummyNode(), 0, comparator);
	}
	
	SortedListHeap(Node<V> dummyNode, int size, Comparator<? super V> comparator) {
		this.dummyNode = dummyNode;
		this.size = size;
		this.comparator = comparator;
	}
	
	@Override
	protected boolean offerNotNull(V val) {
		dummyNode.createNodeAndInsertBefore(val);
		++size;
		moveTowardsHead(dummyNode.getPrevious());
		++modCount;
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public V poll() {
		Node<V> first = dummyNode.getNext();
		if (first == dummyNode) {
			throw new NoSuchElementException();
		}
		first.remove();
		--size;
		++modCount;
		return first.getValue();
	}

	/** {@inheritDoc} */
	@Override
	public V peek() {
		Node<V> first = dummyNode.getNext();
		if (first == dummyNode) {
			return null;
		}
		return first.getValue();
	}

	/** {@inheritDoc} */
	@Override
	public int size() {
		return size;
	}
	
	private void moveTowardsHead(Node<V> node) {
		assert node != dummyNode;	// Can't be the terminator
		
		Node<V> prev = node.getPrevious();
		while (prev != dummyNode) {  // do NOT compare with the dummy node!
			int cmp = comparator.compare(node.getValue(), prev.getValue());
			if (cmp < 0) {
				prev.swapWithNext();
			} else {
				// Found the right location
				break;
			}
			prev = node.getPrevious();
		};
	}
	
	private void moveTowardsTail(Node<V> node) {
		assert node != dummyNode;	// Can't be the terminator
		
		Node<V> next = node.getNext();
		while (next != dummyNode) {  // do NOT compare with the dummy node!
			int cmp = comparator.compare(node.getValue(), next.getValue());
			if (cmp > 0) {
				node.swapWithNext();
			} else {
				// Found the right location
				break;
			}
			next = node.getNext();
		};
	}

	private class HeapIteratorImpl implements HeapIterator<V> {
		
		private Node<V> nextNode = dummyNode.getNext();
		private int expectedModCount = modCount;
		private Node<V> lastReturned = null;
		
		/*
		 * Invoking replace() may move the current item--which we've already iterated over--towards the tail of the
		 * list, in which case we should not show it again. This set helps us track such items.
		 */
		private Set<Node<V>> toSkip = Collections.newSetFromMap(new IdentityHashMap<Node<V>, Boolean>());

		private void advance() {
			do {
				nextNode = nextNode.getNext();
			} while (nextNode != dummyNode && toSkip.contains(nextNode));
		}
		
		/** {@inheritDoc} */
		@Override
		public boolean hasNext() {
			return nextNode != dummyNode;
		}

		/** {@inheritDoc} */
		@Override
		public V next() {
			if (expectedModCount != modCount) {
				throw new ConcurrentModificationException();
			}
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			lastReturned = nextNode;
			advance();
			return lastReturned.getValue();
		}

		/** {@inheritDoc} */
		@Override
		public void remove() {
			if (expectedModCount != modCount) {
				throw new ConcurrentModificationException();
			}
			if (lastReturned == null) {
				throw new IllegalStateException();
			}
			lastReturned.remove();
			--size;
			lastReturned = null;
			expectedModCount = ++modCount;
		}

		/** {@inheritDoc} */
		@Override
		public void replace(V val) {
			if (expectedModCount != modCount) {
				throw new ConcurrentModificationException();
			}
			
			if (lastReturned == null) {
				throw new IllegalStateException();
			}
		
			int compare = comparator.compare(val, lastReturned.getValue());
			lastReturned.setValue(val);
			if (compare < 0) {
				moveTowardsHead(lastReturned);
			} else if (compare > 0) {
				moveTowardsTail(lastReturned);
				toSkip.add(lastReturned);
			} else { } // compare == 0; doesn't move
			lastReturned = null;	// disallow future call to remove() before next()
		}
	}
	
	/** {@inheritDoc} */
	@Override
	public HeapIterator<V> iterator() {
		return new HeapIteratorImpl();
	}
}