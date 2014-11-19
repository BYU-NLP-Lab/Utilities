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

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;

import com.google.common.base.Preconditions;

/**
 * This class provides a skeleton implementation for a Heap in terms of the push, pop, and iterator methods--all of
 * which must be implemented by subclasses.
 * 
 * @author rah67
 *
 */
public abstract class AbstractHeap<V> extends AbstractQueue<V> implements Heap<V> {

	@Override
	// Necessary to avoid compile-time errors.
	public abstract HeapIterator<V> iterator();

	/** {@inheritDoc} */
	@Override
	public V offerThenPoll(V val) {
		offer(val);
		return poll();
	}

	/** {@inheritDoc} */
	@Override
	public void merge(Heap<? extends V> other) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public final boolean offer(V val) {
		Preconditions.checkNotNull(val);
		return offerNotNull(val);
	}
	
	/** Internal implementation of offer. Only invoked with non-null values of val. **/
	protected abstract boolean offerNotNull(V val);

	
	/** {@inheritDoc} */
	@Override
	public boolean replace(V oldVal, V newVal) {
		Preconditions.checkNotNull(oldVal);
		Preconditions.checkNotNull(newVal);
		
		HeapIterator<V> it = iterator();
		V next = null;
		while (it.hasNext() && (!oldVal.equals(next = it.next())));
		if (next == null) {  // valid because heaps can't hold null values
			return false;
		}
		it.replace(newVal);
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public void drainTo(Collection<? super V> coll) {
		while (!isEmpty()) {
			coll.add(poll());
		}
	}
	
	/** {@inheritDoc} */
	@Override
	public void drainTo(Collection<? super V> coll, int maxElements) {
		for (int i = 0; i < maxElements && !isEmpty(); i++) {
			coll.add(poll());
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		Iterator<V> it = iterator();
		if (it.hasNext()) {
			sb.append(it.next());
		}
		while (it.hasNext()) {
			sb.append(", ");
			sb.append(it.next());
		}
		sb.append(']');
		return sb.toString();
	}
}