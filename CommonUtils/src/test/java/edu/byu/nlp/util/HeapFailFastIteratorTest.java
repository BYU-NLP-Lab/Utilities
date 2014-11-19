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

import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.ImmutableList;

import edu.byu.nlp.util.Heap.HeapIterator;

/**
 * Tests whether the iterators from certain heaps properly exhibit fail-fast behavior.
 * 
 * @author rah67
 *
 */
@RunWith(Parameterized.class)
public class HeapFailFastIteratorTest {

	private final HeapFactory<Integer> factory;
	
	public HeapFailFastIteratorTest(HeapFactory<Integer> factory) {
		this.factory = factory;
	}
	
	@Parameters
	public static Collection<Object[]> heapFactories() {
		return ImmutableList.of(
				new Object[]{ HeapFactories.binaryHeapFactory() },
				new Object[]{ HeapFactories.sortedListHeapFactory() }
		);
	}
	
	/**
	 * Ensures that the iterator is fail-fast.
	 */
	@Test
	public void testIteratorIsFailFast() {
		Heap<Integer> heap = factory.newHeap(Arrays.asList(7, 3, 3, 5, 3, 9, 1));
		HeapIterator<Integer> it = heap.iterator();
		heap.offer(2);
		try {
			it.next();
			fail();
		} catch (ConcurrentModificationException expected) { }
		
		heap.offer(2);
		it = heap.iterator();
		it.next();
		heap.poll();
		try {
			it.next();
			fail();
		} catch (ConcurrentModificationException expected) { }
		
		it = heap.iterator();
		it.next();
		it.next();
		heap.offerThenPoll(13);
		try {
			it.next();
			fail();
		} catch (ConcurrentModificationException expected) { }
	}
}