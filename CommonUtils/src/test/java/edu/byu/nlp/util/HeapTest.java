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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.ImmutableList;

/**
 * @author rah67
 *
 */
@RunWith(Parameterized.class)
public class HeapTest {

	private final HeapFactory<Integer> factory;
	
	public HeapTest(HeapFactory<Integer> factory) {
		this.factory = factory;
	}
	
	@Parameters
	public static Collection<Object[]> heapFactories() {
		return HeapTests.heapFactories();
	}
	
	private Heap<Integer> newHeap() {
		return factory.newHeap(ImmutableList.<Integer>of());
	}
	
	private Heap<Integer> newHeapFromValues(Integer... values) {
		return factory.newHeap(ImmutableList.<Integer>copyOf(values));
	}
	
	/**
	 * Test method for {@link edu.byu.nlp.util.Heap#isEmpty()}.
	 */
	public void testIsEmpty() {
		Heap<Integer> heap = newHeap();
		assertTrue(heap.isEmpty());
		assertTrue(heap.isEmpty());
		assertTrue(heap.offer(1));
		assertFalse(heap.isEmpty());
		assertFalse(heap.isEmpty());
		assertTrue(heap.offer(2));
		assertFalse(heap.isEmpty());
		assertTrue(heap.offer(1));
		assertFalse(heap.isEmpty());
		heap.poll();
		assertFalse(heap.isEmpty());
		heap.poll();
		assertFalse(heap.isEmpty());
		heap.poll();
		assertTrue(heap.isEmpty());
		assertTrue(heap.isEmpty());
		
		heap = newHeapFromValues(1);
		assertFalse(heap.isEmpty());
		assertFalse(heap.isEmpty());
		heap.poll();
		assertTrue(heap.isEmpty());
		assertTrue(heap.isEmpty());
	}

	/**
	 * Test method for {@link edu.byu.nlp.util.BinaryHeap#size()}.
	 */
	@Test
	public void testSize() {
		Heap<Integer> heap = newHeap();
		assertThat(heap.size(), is(equalTo(0)));
		assertTrue(heap.offer(1));
		assertThat(heap.size(), is(equalTo(1)));
		// Two calls for good measure (i.e., if size() has side-effects).
		assertThat(heap.size(), is(equalTo(1)));
		assertTrue(heap.offer(2));
		assertThat(heap.size(), is(equalTo(2)));
		assertTrue(heap.offer(1));
		assertThat(heap.size(), is(equalTo(3)));
		heap.poll();
		assertThat(heap.size(), is(equalTo(2)));
		// Two calls for good measure (i.e., if size() has side-effects).
		assertThat(heap.size(), is(equalTo(2)));
		heap.poll();
		assertThat(heap.size(), is(equalTo(1)));
		heap.poll();
		assertThat(heap.size(), is(equalTo(0)));

		heap = newHeapFromValues(1);
		assertThat(heap.size(), is(equalTo(1)));
		// Two calls for good measure (i.e., if size() has side-effects).
		assertThat(heap.size(), is(equalTo(1)));
		heap.poll();
		assertThat(heap.size(), is(equalTo(0)));
		// Two calls for good measure (i.e., if size() has side-effects).
		assertThat(heap.size(), is(equalTo(0)));
	}

	/**
	 * Test method for {@link edu.byu.nlp.util.BinaryHeap#offer(java.lang.Comparable)}.
	 */
	@Test
	public void testPush() {
		final List<Integer> list = ImmutableList.of(5, 1, 3, 7, 3, 4, 10, 0, 1);
		Heap<Integer> heap = newHeap();
		for (Integer val : list) {
			assertTrue(heap.offer(val));
		}
		
		List<Integer> drainedHeap = Heaps.drain(heap);
		assertThat(drainedHeap, is(equalTo(Collections3.sortedCopyOf(list))));
	}

	/**
	 * Test method for {@link edu.byu.nlp.util.BinaryHeap#poll()}.
	 */
	@Test
	public void testPop() {
		final List<Integer> list = ImmutableList.of(5, 1, 3, 7, 3, 4, 10, 0, 1);
		Heap<Integer> heap = factory.newHeap(list);
		final List<Integer> expected = ImmutableList.copyOf(Collections3.sortedCopyOf(list));
		
		for (Integer i : expected) {
			assertThat(heap.poll(), is(equalTo(i)));
		}
		
		try {
			heap.poll();
			fail();
		} catch (NoSuchElementException expectedException) { }
	}

	/**
	 * Test method for {@link edu.byu.nlp.util.BinaryHeap#peek()}.
	 */
	@Test
	public void testPeek() {
		final List<Integer> list = ImmutableList.of(5, 1, 3, 7, 3, 4, 10, 0, 1);
		Heap<Integer> heap = factory.newHeap(list);
		final List<Integer> expected = ImmutableList.copyOf(Collections3.sortedCopyOf(list));
		
		for (Integer i : expected) {
			assertThat(heap.peek(), is(equalTo(i)));
			heap.poll();
		}

		assertThat(heap.peek(), is(nullValue()));
	}

	/**
	 * Test method for {@link edu.byu.nlp.util.BinaryHeap#offerThenPoll(java.lang.Comparable)}.
	 */
	@Test
	public void testPushThenPop() {
		Heap<Integer> heap = newHeap();
		Integer thirteen = new Integer(13);
		assertSame(thirteen, heap.offerThenPoll(thirteen));
		
		assertTrue(heap.offer(10));
		assertTrue(heap.offer(15));
		assertTrue(heap.offer(20));
		
		// Heap: [10, 15, 20]
		assertThat(heap.offerThenPoll(9), is(equalTo(9)));

		// Heap: [10, 15, 20]
		assertThat(heap.offerThenPoll(11), is(equalTo(10)));
		
		// Heap: [11, 15, 20]
		assertThat(heap.offerThenPoll(21), is(equalTo(11)));

		// Heap: [15, 20, 21]
		Collection<Integer> drainedHeap = Heaps.drain(heap);
		Collection<Integer> sortedValues = ImmutableList.of(15, 20, 21);
		assertThat(drainedHeap, is(equalTo(sortedValues)));
	}

	@Test(expected = NullPointerException.class)
	public void testOfferNullThrowsNPE() {
		Heap<Integer> heap = newHeap();
		heap.offer(null);
	}
	
	@Test(expected = NullPointerException.class)
	public void testReplaceNullWithXThrowsNPE() {
		Heap<Integer> heap = newHeap();
		assertTrue(heap.offer(1));
		heap.replace(null, 2);
	}
	
	@Test(expected = NullPointerException.class)
	public void testReplaceXWithNullThrowsNPE() {
		Heap<Integer> heap = newHeap();
		assertTrue(heap.offer(1));
		heap.replace(1, null);
	}

	@Test
	public void testReplace() {
		Heap<Integer> heap = newHeap();
		assertFalse(heap.replace(1, 7));
		assertTrue(heap.offer(1));
		assertTrue(heap.replace(1, 7));
		assertTrue(heap.offer(1));
		assertTrue(heap.replace(1, 3));
		assertTrue(heap.offer(1));
		assertTrue(heap.replace(1, 4));
		assertTrue(heap.offer(6));
		assertTrue(heap.replace(3, 5));
		assertTrue(heap.replace(5, 11));
		assertTrue(heap.offer(1));
		
		Collection<Integer> drainedHeap = Heaps.drain(heap);
		Collection<Integer> sortedValues = ImmutableList.of(1, 4, 6, 7, 11);
		assertThat(drainedHeap, is(equalTo(sortedValues)));
	}
	
	/**
	 * Test method for {@link edu.byu.nlp.util.BinaryHeap#merge(edu.byu.nlp.util.Heap)}.
	 */
	@Test
	public void testMerge() {
		fail("Not yet implemented"); // TODO
	}
	
}