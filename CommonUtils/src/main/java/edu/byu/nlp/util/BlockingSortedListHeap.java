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
import java.util.IdentityHashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

/**
 * An unbounded blocking heap backed by a sorted linked list. While the pop operation returns null if the list is empty,
 * the take() method blocks until data is available. Like most concurrent collections, the iterator for this class is
 * weakly consistent.
 * 
 * @author rah67
 *
 */
public class BlockingSortedListHeap<V> extends AbstractHeap<V> implements BlockingHeap<V> {

	private final Lock lock = new ReentrantLock();
	private final Condition notEmpty = lock.newCondition();
	
	private final Comparator<? super V> comparator;
	
	/**
	 * This dummy node allows us to avoid special-case code for operations on head and tail. The dummy node is ALWAYS
	 * head AND tail of the list. dummyNode.next points to the first REAL node if there is one or to itself if
	 * the list is empty. dummyNode.previous points to the last REAL node, if there is one; otherwise, it points to
	 * itself.
	 */
	private final VolatileNode<V> dummyNode;
	private volatile int size;  // protected by lock

	/**
	 * The Iterable is copied. 
	 */
	public static <V extends Comparable<? super V>> BlockingSortedListHeap<V> from(Iterable<? extends V> it) {
		return from(it, Ordering.<V>natural());
	}

	public static <V> BlockingSortedListHeap<V> from(Iterable<? extends V> it, Comparator<? super V> comparator) {
		List<V> sorted = Collections3.sortedCopyOf(it, comparator);
		return buildListFromSorted(sorted, comparator);
	}

    private static <V> BlockingSortedListHeap<V> buildListFromSorted(List<V> sorted, Comparator<? super V> comparator) {
        VolatileNode<V> dummy = VolatileNode.newDummyNode();
		for (V val : sorted) {
			// Given the way dummy is defined, this adds to the end of the list.
			dummy.createNodeAndInsertBefore(val);
		}
		return new BlockingSortedListHeap<V>(dummy, sorted.size(), comparator);
    }
	
	public static class ScoredItem<V> {

	    public static class SIComparator<V> implements Comparator<ScoredItem<V>> {

            /** {@inheritDoc} */
            @Override
            public int compare(ScoredItem<V> si1, ScoredItem<V> si2) {
                return Double.compare(si1.score, si2.score);
            }
	        
	    }
	    
	    private final V item;
	    private final double score;

	    public ScoredItem(V item, double score) {
            this.item = item;
            this.score = score;
        }

        public V getItem() {
            return item;
        }

        public double getScore() {
            return score;
        }

        public static <V> Comparator<ScoredItem<V>> comparator() {
            return new SIComparator<V>();
        }
        
        public static <V> ScoredItem<V> from(V item, double score) {
            return new ScoredItem<V>(item, score);
        }

        @Override
        public String toString() {
            return "ScoredItem [item=" + item + ", score=" + score + "]";
        }
	}
	
	/**
	 * Uses default value of minFirst = true 
	 */
	public static <V> BlockingSortedListHeap<ScoredItem<V>> usingScored(Iterable<? extends V> it) {
	    return usingScored(it, true);
	}

    public static <V> BlockingSortedListHeap<ScoredItem<V>> usingScored(Iterable<? extends V> it, boolean minFirst) {
        List<ScoredItem<V>> instances = Lists.newArrayList();
        for (V instance : it) {
            instances.add(ScoredItem.from(instance, 0.0));
        }
        return buildListFromSorted(instances, ScoredItem.<V>comparator());
    }
    
	public BlockingSortedListHeap(Comparator<? super V> comparator) {
		this.dummyNode = VolatileNode.newDummyNode();
		this.size = 0;
		this.comparator = comparator;
	}
	
	BlockingSortedListHeap(VolatileNode<V> dummyNode, int size, Comparator<? super V> comparator) {
		this.dummyNode = dummyNode;
		this.size = size;
		this.comparator = comparator;
	}
	
	/** {@inheritDoc} */
	@Override
	public int size() {
		lock.lock();
		try {
			return size;
		} finally {
			lock.unlock();
		}
	}
	
	@Override
	protected boolean offerNotNull(V val) {
		putSilently(val);
		return true;
	}

	private void putSilently(V val) {
		lock.lock();
		try {
			dummyNode.createNodeAndInsertBefore(val);
			++size;
			moveTowardsHead(dummyNode.getPrevious());
			notEmpty.signal();
		} finally {
			lock.unlock();
		}
	}
	
	@Override
	public void put(V val) throws InterruptedException {
		putSilently(val);
	}

	/** {@inheritDoc} */
	@Override
	public V peek() {
		lock.lock();
		try {
			VolatileNode<V> first = dummyNode.getNext();
			if (first == dummyNode) {
				return null;
			}
			return first.getValue();
		} finally {
			lock.unlock();
		}
	}

	/** {@inheritDoc} */
	@Override
	public V poll() {
		lock.lock();
		return doPoll();
	}

    private V doPoll() {
        try {
			VolatileNode<V> first = dummyNode.getNext();
			if (first == dummyNode) {
			    return null;
			}
			first.remove();
			--size;
			return first.getValue();
		} finally {
			lock.unlock();
		}
    }
	
	@Override
    /** {@inheritDoc} */
    public V poll(long timeout, TimeUnit timeUnit) throws InterruptedException {
	    if (!lock.tryLock(timeout, timeUnit)) {
	        return null;
	    }
        return doPoll();
    }

    /**
	 * Like pop(), but blocks if the heap is empty.
	 *  
	 * @throws InterruptedException
	 */
	public V take() throws InterruptedException {
		lock.lock();
		try {
			VolatileNode<V> first = dummyNode.getNext();
			while (first == dummyNode) {
				notEmpty.await();
				first = dummyNode.getNext();
			}
			first.remove();
			--size;
			return first.getValue();
		} finally {
			lock.unlock();
		}
	}
	
	/** {@inheritDoc} */
	@Override
	public synchronized boolean isEmpty() {
		lock.lock();
		try {
			return size == 0;
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * This implementation is atomic, but non-blocking.
	 */
	@Override
	public V offerThenPoll(V val) {
		lock.lock();
		try {
			if (size == 0) {
				return val;
			}
			dummyNode.createNodeAndInsertAfter(val);
			moveTowardsTail(dummyNode.getNext());
			VolatileNode<V> firstNode = dummyNode.getNext();
			assert(firstNode != null);
			firstNode.remove();
			return firstNode.getValue();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Atomically takes and then puts the specified value, blocking if necessary.
	 */
	public V takeThenPut(V val) throws InterruptedException {
		lock.lock();
		try {
			if (size == 0) {
				return val;
			}
			VolatileNode<V> firstNode = dummyNode.getNext();
			while (firstNode == dummyNode) {
				notEmpty.await();
				firstNode = dummyNode.getNext();
			}
			V firstVal = firstNode.getValue();
			firstNode.setValue(val);

			int compare = comparator.compare(val, firstVal);
			if (compare > 0) {
				moveTowardsTail(firstNode);
			}  // else, it's already the smallest and the head, so don't move it!
			return firstVal;
		} finally {
			lock.unlock();
		}
	}

	private void moveTowardsHead(VolatileNode<V> node) {
		assert node != dummyNode;	// Can't be the terminator
		
		VolatileNode<V> prev = node.getPrevious();
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
	
	private void moveTowardsTail(VolatileNode<V> node) {
		assert node != dummyNode;	// Can't be the terminator
		
		VolatileNode<V> next = node.getNext();
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

		private VolatileNode<V> nextNode = dummyNode.getNext();
		private VolatileNode<V> lastReturned = null;	// necessary for remove() and replace()
		
		/*
		 * Invoking replace() may move the current item--which we've already iterated over--towards the tail of the
		 * list, in which case we should not show it again. This set helps us track such items.
		 */
		private Set<VolatileNode<V>> toSkip = Collections.newSetFromMap(new IdentityHashMap<VolatileNode<V>, Boolean>());
		
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
			lock.lock();
			try {
				if (lastReturned == null) {
					throw new IllegalStateException();
				}
				lastReturned.remove();
				--size;
				lastReturned = null;
			} finally {
				lock.unlock();
			}
		}

		/** {@inheritDoc} */
		@Override
		public void replace(V val) {
			lock.lock();
			try {
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
			} finally {
				lock.unlock();
			}
		}
	}
	
	/** {@inheritDoc} */
	@Override
	public HeapIterator<V> iterator() {
		return new HeapIteratorImpl();
	}
}