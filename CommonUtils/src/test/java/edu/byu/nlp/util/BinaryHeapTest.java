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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import edu.byu.nlp.util.Heap.HeapIterator;
import edu.byu.nlp.util.asserts.MoreAsserts;

/**
 * @author rah67
 *
 */
public class BinaryHeapTest {

	/**
	 * Test method for {@link edu.byu.nlp.util.BinaryMinHeap#push()}.
	 */
	@Test
	public void testPushMaintainsBinaryHeap() {
		Heap<Integer> heap = Heaps.newBinaryMinHeap();
		heap.offer(5);
		assertEquals(ImmutableList.of(5), ImmutableList.copyOf(heap));
		
		heap.offer(1);
		assertEquals(ImmutableList.of(1, 5), ImmutableList.copyOf(heap));
		
		Integer three1 = new Integer(3);
		heap.offer(three1);
		assertEquals(ImmutableList.of(1, 5, 3), ImmutableList.copyOf(heap));
		
		heap.offer(7);
		assertEquals(ImmutableList.of(1, 5, 3, 7), ImmutableList.copyOf(heap));

		Integer three2 = new Integer(3);
		heap.offer(three2);
		assertEquals(ImmutableList.of(1, 3, 3, 7, 5), ImmutableList.copyOf(heap));

		assert(three1 != three2);
		List<Integer> heapCopy = ImmutableList.copyOf(heap);
		Assert.assertSame(three1, heapCopy.get(2));
		Assert.assertSame(three2, heapCopy.get(1));
		
		heap.offer(4);
		assertEquals(ImmutableList.of(1, 3, 3, 7, 5, 4), ImmutableList.copyOf(heap));

		heap.offer(10);
		assertEquals(ImmutableList.of(1, 3, 3, 7, 5, 4, 10), ImmutableList.copyOf(heap));

		heap.offer(0);
		assertEquals(ImmutableList.of(0, 1, 3, 3, 5, 4, 10, 7), ImmutableList.copyOf(heap));

		heap.offer(1);
		assertEquals(ImmutableList.of(0, 1, 3, 1, 5, 4, 10, 7, 3), ImmutableList.copyOf(heap));
	}
	
	/**
	 * Test method for {@link edu.byu.nlp.util.BinaryMinHeap#iterator()}.
	 */
	@Test
	public void testIteratorReplaceNewValueComesLaterInHeap() {
		final int indexToReplace = 0;
		final int valueBeforeReplace = 1;
		final int valueAfterReplace = 103;
		final List<Integer> initialHeap = ImmutableList.of(1, 2, 100, 3, 4, 105, 110, 6, 7);
		Heap<Integer> heap = Heaps.newBinaryMinHeapFrom(initialHeap);

		List<Integer> seen = Lists.newArrayList();
		
		HeapIterator<Integer> it = heap.iterator();
		
		for (int i = 0; i < indexToReplace; i++) {
			assert(it.hasNext());
			seen.add(it.next());
		}
		
		Integer next = it.next();
		assert(next.equals(valueBeforeReplace));
		it.replace(valueAfterReplace);
		seen.add(next);
		
		while (it.hasNext()) {
			seen.add(it.next());
		}
		
		// The order of iteration is not important; the only requirement is to see all of the values.
		MoreAsserts.assertEqualsUnordered(initialHeap, seen);
		
		// But the values should come out of the Heap in order.
		ArrayList<Integer> expected = Lists.newArrayList(initialHeap);
		expected.set(indexToReplace, valueAfterReplace);
		
		assertEquals(Collections3.sortedCopyOf(expected), Heaps.drain(heap));
	}

	/**
	 * Test method for {@link edu.byu.nlp.util.BinaryHeap#iterator()}.
	 */
	@Test
	public void testIteratorRemoveCausesSkip() {
		final int firstIndexToRemove = 5;
		final int firstValueToRemove = 105;
		final int secondIndexToRemove = 6;		// ORIGINAL index, i.e., before the first one is removed
		final int secondValueToRemove = 110;
		
		// Notice that 6 is not a direct descendant of 105 in the following binary tree.
		// This should cause 6 to be added to the "skipped" list internally. If it isn't then, either
		// The heap invariant will be broken and we shouldn't get a sorted list at the end (or something
		// will break) OR the iterator won't see the 6.
		final List<Integer> list = ImmutableList.of(1, 2, 100, 3, 4, 105, 110, 5, 6);
		Heap<Integer> heap = Heaps.newBinaryMinHeapFrom(list);
		// This list is already heapified
		assert(list.equals(ImmutableList.copyOf(heap)));

		List<Integer> seen = Lists.newArrayList();
		
		Iterator<Integer> it = heap.iterator();

		for (int i = 0; i < firstIndexToRemove; i++) {
			assert(it.hasNext());
			seen.add(it.next());
		}

		Integer next = it.next();
		assert(next.equals(firstValueToRemove));
		it.remove();
		seen.add(next);
		
		for (int i = firstIndexToRemove + 1; i < secondIndexToRemove; i++) {
			assert(it.hasNext());
			seen.add(it.next());
		}
		
		next = it.next();
		assert(next.equals(secondValueToRemove));
		it.remove();
		seen.add(next);

		Iterators.addAll(seen, it);
		
		assertThat(seen, hasSameElementsAs(list));
		
		List<Integer> expected = Lists.newArrayList(seen);
		expected.remove(secondIndexToRemove);
		expected.remove(firstIndexToRemove);
		Collections.sort(expected);
		
		List<Integer> drained = Heaps.drain(heap);
		
		assertThat(drained, is(equalTo(expected)));
	}
}