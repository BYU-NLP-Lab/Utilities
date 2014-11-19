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
import java.util.Iterator;

/**
 * A heap is a tree-like structure in which each node in the tree is less-than-or-equal-to (or greater-than-or-equal-to)
 * it's parent. Heaps do not allow null values.
 * 
 * @author rah67
 *
 * @param <V>
 */
public interface Heap<V> extends Iterable<V> {

	static interface HeapIterator<V> extends Iterator<V> {
		/**
		 * Pops the current value and pushes the new one onto the heap. This effectively allows for promotion/demotion.
		 * This method may not be called after remove() and may only be called once.
		 * 
		 *  @throws IllegalStateException if invoked multiple times or after a call to remove().
		 */
		void replace(V val);
	}
	
	/**
	 * Insert.
	 * 
	 * @throws NullPointerException of val is null.
	 */
	boolean offer(V val);

	/** Delete-min (max) / extract-min (max) **/
	V poll();

	/** Find-min (max) **/
	V peek();

	/** Insert followed by delete-min **/
	V offerThenPoll(V val);

	/** Merges the specified heap into this heap **/
	void merge(Heap<? extends V> other);

	/**
	 * Promotion/demotion. Returns false if oldVal wasn't found.
	 *
	 * @throws NullPointerException if either oldVal or newVal is false.
	 */
	// TODO(rah67): add unit test.
	boolean replace(V oldVal, V newVal);
	
	//
	// Collections-like interface
	//
	boolean isEmpty();

	int size();

	@Override
	HeapIterator<V> iterator();
	
	boolean remove(Object o);
	
	//
	// Interface-with collections
	//
	void drainTo(Collection<? super V> coll);
	void drainTo(Collection<? super V> coll, int maxElements);
}