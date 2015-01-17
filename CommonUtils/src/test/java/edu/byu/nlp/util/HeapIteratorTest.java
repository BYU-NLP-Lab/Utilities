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

import static edu.byu.nlp.util.asserts.MoreMatchers.hasSameElementsAs;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import edu.byu.nlp.util.Heap.HeapIterator;

/**
 * @author rah67
 *
 */
@RunWith(Parameterized.class)
public class HeapIteratorTest {

	private final HeapFactory<Integer> factory;
	
	public HeapIteratorTest(HeapFactory<Integer> factory) {
		this.factory = factory;
	}
	
	@Parameters
	public static Collection<Object[]> heapFactories() {
		return HeapTests.heapFactories();
	}
	
	/**
	 * Ensures that the iterator returns the values contained in the heap.
	 */
	@Test
	public void testIterator() {
		List<Integer> values = ImmutableList.of(7, 3, 3, 5, 3, 9, 1);
		Heap<Integer> heap = factory.newHeap(values);
		List<Integer> seen = Lists.newArrayList(heap);
		assertThat(seen, hasSameElementsAs(values));
	}
	
	/**
	 * Test method for {@link edu.byu.nlp.util.BinaryHeap#iterator()}.
	 */
	@Test
	public void testIteratorRemove() {
		final List<Integer> list = ImmutableList.of(1, 2, 100, 3, 4, 105, 110, 5, 6);
		for (int indexToReplace = 0; indexToReplace < list.size(); indexToReplace++) {
			Heap<Integer> heap = factory.newHeap(list);
			HeapIterator<Integer> it = heap.iterator();
			List<Integer> seen = Lists.newArrayList(Iterators.limit(it, indexToReplace));
			Integer removed = it.next();
			seen.add(removed);
			it.remove();
			Iterators.addAll(seen, it);
			// Removal should not affect the elements we saw during iteration
			assertThat(seen, hasSameElementsAs(list));
			
			// But it should have been properly removed from the heap
			List<Integer> expected = Collections3.sortedCopyOf(list);
			expected.remove(removed);
			List<Integer> drained = Heaps.drain(heap);
			assertThat(drained, is(equalTo(expected)));
		}
	}

	/**
	 * Test method for {@link edu.byu.nlp.util.BinaryHeap#iterator()}.
	 */
	@Test
	public void testIteratorRemoveTwoElements() {
		final int firstToRemove = 5;
		final int secondToRemove = 6;		// ORIGINAL index, i.e., before the first one is removed
		
		final List<Integer> list = ImmutableList.of(1, 2, 100, 3, 4, 105, 110, 5, 6);
		Heap<Integer> heap = factory.newHeap(list);

		List<Integer> seen = Lists.newArrayList();
		
		Iterator<Integer> it = heap.iterator();

		Iterators.addAll(seen, Iterators.limit(it, firstToRemove));

		Integer first = it.next();
		it.remove();
		seen.add(first);
		
		Iterators.addAll(seen, Iterators.limit(it, secondToRemove - (firstToRemove + 1)));
		
		Integer second = it.next();
		it.remove();
		seen.add(second);

		Iterators.addAll(seen, it);
		
		assertThat(seen, hasSameElementsAs(list));
		
		// Check that they were actually removed in the final heap.
		List<Integer> expected = Collections3.sortedCopyOf(list);
		expected.remove(first);
		expected.remove(second);
		
		List<Integer> drained = Heaps.drain(heap);
		assertThat(drained, is(equalTo(expected)));
	}

	/**
	 * Test method for {@link edu.byu.nlp.util.BinaryHeap#iterator()}.
	 */
	@Test
	// TODO(rah67): convert to parameterized JUnit test?
	public void testIteratorReplaceWithGreater() {
		final List<Integer> values = ImmutableList.of(1, 100, 4, 3, 7, 105, 110, 6, 2);
		for (int indexToReplace = 0; indexToReplace < values.size(); indexToReplace++) {
			Heap<Integer> heap = factory.newHeap(values);
			HeapIterator<Integer> it = heap.iterator();
			List<Integer> seen = Lists.newArrayList(Iterators.limit(it, indexToReplace));

			Integer val = it.next();
			seen.add(val);
			it.replace(val * 3 + 13);

			Iterators.addAll(seen, it);
			
			// The order of iteration is not important; the only requirement is to see all of the values.
			assertThat(seen, hasSameElementsAs(values));
			
			// But the values should still come out of the Heap in order.
			List<Integer> expected = Lists.newArrayList(values);
			expected.set(expected.indexOf(val), seen.get(indexToReplace) * 3 + 13);
			Collections.sort(expected);
			List<Integer> drainedHeap = Heaps.drain(heap);
			assertThat(drainedHeap, is(equalTo(expected)));
		}
	}

	@Test
	// TODO(rah67): convert to parameterized JUnit test?
	public void testIteratorReplaceWithLess() {
		final List<Integer> values = ImmutableList.of(1, 100, 4, 3, 7, 105, 110, 6, 2);
		for (int indexToReplace = 0; indexToReplace < values.size(); indexToReplace++) {
			Heap<Integer> heap = factory.newHeap(values);
			HeapIterator<Integer> it = heap.iterator();
			List<Integer> seen = Lists.newArrayList(Iterators.limit(it, indexToReplace));

			Integer val = it.next();
			seen.add(val);
			it.replace(-val / 3 - 11);

			Iterators.addAll(seen, it);
			
			// The order of iteration is not important; the only requirement is to see all of the values.
			assertThat(seen, hasSameElementsAs(values));
			
			// But the values should still come out of the Heap in order.
			List<Integer> expected = Lists.newArrayList(values);
			expected.set(expected.indexOf(val), -seen.get(indexToReplace) / 3 - 11);
			Collections.sort(expected);
			List<Integer> drainedHeap = Heaps.drain(heap);
			assertThat(drainedHeap, is(equalTo(expected)));
		}
	}
	
	@Test
	public void testIteratorReplaceWithSame() {
		final List<Integer> values = ImmutableList.of(1, 100, 4, 3, 7, 105, 110, 6, 2);
		for (int indexToReplace = 0; indexToReplace < values.size(); indexToReplace++) {
			Heap<Integer> heap = factory.newHeap(values);
			HeapIterator<Integer> it = heap.iterator();
			List<Integer> seen = Lists.newArrayList(Iterators.limit(it, indexToReplace));

			Integer val = it.next();
			seen.add(val);
			it.replace(new Integer(val));

			Iterators.addAll(seen, it);
			
			// The order of iteration is not important; the only requirement is to see all of the values.
			assertThat(seen, hasSameElementsAs(values));
			
			// But the values should still come out of the Heap in order.
			List<Integer> expected = Lists.newArrayList(values);
			Collections.sort(expected);
			List<Integer> drainedHeap = Heaps.drain(heap);
			assertThat(drainedHeap, is(equalTo(expected)));
		}
	}
	
	@Test(expected = IllegalStateException.class)
	public void testRemoveOnlyValidOnce() {
		Heap<Integer> heap = factory.newHeap(Arrays.asList(1, 2, 3, 4));
		HeapIterator<Integer> it = heap.iterator();
		it.next();
		it.next();
		it.remove();
		it.remove();
	}
	
	@Test(expected = IllegalStateException.class)
	public void testNextMustBeInvokedBeforeRemove() {
		Heap<Integer> heap = factory.newHeap(Arrays.asList(1, 2, 3, 4));
		HeapIterator<Integer> it = heap.iterator();
		it.remove();
	}
	
	@Test(expected = IllegalStateException.class)
	public void testReplaceOnlyValidOnce() {
		Heap<Integer> heap = factory.newHeap(Arrays.asList(1, 2, 3, 4));
		HeapIterator<Integer> it = heap.iterator();
		it.next();
		it.next();
		it.replace(5);
		it.replace(5);
	}
	
	@Test(expected = IllegalStateException.class)
	public void testReplaceInvalidAfterRemove() {
		Heap<Integer> heap = factory.newHeap(Arrays.asList(1, 2, 3, 4));
		HeapIterator<Integer> it = heap.iterator();
		it.next();
		it.next();
		it.remove();
		it.replace(5);
	}
	
	@Test(expected = NoSuchElementException.class)
	public void testNextThrowsNoSuchElementExceptionWhenHeapIsEmpty() {
		Heap<Integer> heap = factory.newHeap(Arrays.asList(1, 2, 3, 4));
		HeapIterator<Integer> it = heap.iterator();
		Iterators.getLast(it);
		assertThat(it.hasNext(), is(false));
		it.next();
	}

	@Test
	public void testIteratorReplaceSkipsSameElementTwice() {
		List<Integer> values = ImmutableList.of(1, 10, 20, 30);
		Heap<Integer> heap = factory.newHeap(values);
		HeapIterator<Integer> it = heap.iterator();
		
		List<Integer> seen = Lists.newArrayList();
		
		// Ensure the replacements have the same identity
		Integer twentyFive = new Integer(25);
		
		seen.add(it.next());
		it.replace(twentyFive);
		seen.add(it.next());
		it.replace(twentyFive);
		Iterators.addAll(seen, it);
		
		// The two (identical) replacements should not have been seen.
		assertThat(seen, hasSameElementsAs(values));
		
		// But they should still be on the heap.
		// We use the original iteration order and then apply the replacements.
		List<Integer> expected = Lists.newArrayList(seen);
		expected.set(0, twentyFive);
		expected.set(1, twentyFive);
		Collections.sort(expected);
		
		List<Integer> drainedHeap = Heaps.drain(heap);
		assertThat(drainedHeap, is(equalTo(expected)));
	}
	
	@Test
	public void testHasNextAfterDrain() {
		Heap<Integer> heap = factory.newHeap(ImmutableList.of(3, 1));
		assertThat(heap.poll(), is(equalTo(1)));
		assertThat(heap.poll(), is(equalTo(3)));
		Iterator<Integer> it = heap.iterator();
		assertThat(it.hasNext(), is(false));
	}
}