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

/**
 * @author rah67
 *
 */
public class HeapFactories {
	
	private HeapFactories() { }
	
	public static <V extends Comparable<? super V>> HeapFactory<V> synchronizedHeapFactory(final HeapFactory<V> factory) {
		return new HeapFactory<V>() {

			@Override
			public Heap<V> newHeap(Iterable<V> it) {
				return Heaps.synchronizedHeap(factory.newHeap(it));
			}
			
		};
	}
	
	public static <V extends Comparable<? super V>> HeapFactory<V> binaryHeapFactory() {
		return new HeapFactory<V>() {

			@Override
			public Heap<V> newHeap(Iterable<V> it) {
				return Heaps.newBinaryMinHeapFrom(it);
			}
			
		};
	}
	
	public static <V extends Comparable<? super V>> HeapFactory<V> sortedListHeapFactory() {
		return new HeapFactory<V>() {

			@Override
			public Heap<V> newHeap(Iterable<V> it) {
				return SortedListHeap.from(it);
			}
			
		};
	}

	public static <V extends Comparable<? super V>> HeapFactory<V> concurrentSortedListHeapFactory() {
		return new HeapFactory<V>() {

			@Override
			public Heap<V> newHeap(Iterable<V> it) {
				return BlockingSortedListHeap.from(it);
			}
			
		};
	}
}