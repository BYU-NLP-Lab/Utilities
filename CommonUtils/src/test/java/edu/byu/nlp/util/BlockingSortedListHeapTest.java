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
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import edu.byu.nlp.util.Heap.HeapIterator;

/**
 * Tests for concurrency bugs in {@link BlockingSortedHeap}.
 * 
 * @author rah67
 *
 */
public class BlockingSortedListHeapTest {

	private HeapFactory<Integer> factory;

	@Before
	public void setUp() {
		factory = HeapFactories.concurrentSortedListHeapFactory();
	}
	
	private static class Take implements Callable<Integer> {
		private final BlockingSortedListHeap<Integer> heap;
		
		private Take(BlockingSortedListHeap<Integer> heap) {
			this.heap = heap;
		}

		/** {@inheritDoc} */
		@Override
		public Integer call() throws Exception {
			return heap.take();
		}
	}
	
	@Test(expected = TimeoutException.class)
	public void testTakeBlocksOnEmpty() throws InterruptedException, ExecutionException, TimeoutException {
		Heap<Integer> heap = factory.newHeap(ImmutableList.of(3, 1));
		Callable<Integer> take = new Take((BlockingSortedListHeap<Integer>) heap);
		ExecutorService executor = Executors.newSingleThreadExecutor();
		// Drain the queue without contention; helps calibrate whether 100 ms should be enough.
		try {
			Future<Integer> future = executor.submit(take);
			assertThat(future.get(100000, TimeUnit.MILLISECONDS), is(equalTo(1)));
			future = executor.submit(take);
			assertThat(future.get(100000, TimeUnit.MILLISECONDS), is(equalTo(3)));
		} catch (TimeoutException exception) {
			fail("Should not have timed out");
		}
		
		// 100 ms should be enough to determine we blocked.
		Future<Integer> future = executor.submit(take);
		future.get(100, TimeUnit.MILLISECONDS);
	}
	
	private static class Push implements Callable<Void> {
		private final BlockingSortedListHeap<Integer> heap;
		private final Integer val;
		
		private Push(BlockingSortedListHeap<Integer> heap, Integer val) {
			this.heap = heap;
			this.val = val;
		}

		/** {@inheritDoc} */
		@Override
		public Void call() throws Exception {
			heap.offer(val);
			return null;
		}
	}
	
	@Test
	public void testPushUnblocksTake() throws InterruptedException, ExecutionException {
		BlockingSortedListHeap<Integer> heap = (BlockingSortedListHeap<Integer>)
				factory.newHeap(ImmutableList.<Integer>of());
		ExecutorService executor = Executors.newFixedThreadPool(2);

		Take take = new Take(heap);
		Future<Integer> takeFuture = executor.submit(take);
		try {
			assertThat(heap.isEmpty(), is(true));
			// Should block; heap is empty;
			takeFuture.get(100, TimeUnit.MILLISECONDS);
			fail();
		} catch (TimeoutException expected) { }
		
		Integer val = 5;
		Push push = new Push(heap, val);
		Future<Void> pushFuture = executor.submit(push);
		try {
			// Should not block
			pushFuture.get(1000000, TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			fail();
		}
		
		try {
			// Should not block now that there actually is a value.
			assertThat(takeFuture.get(100, TimeUnit.MILLISECONDS), is(equalTo(val)));
		} catch (TimeoutException e) {
			fail();
		}
		assertThat(heap.isEmpty(), is(true));
	}
	
	@Test
	public void testIteratorCanRemovePoppedItem() {
		Heap<Integer> heap = factory.newHeap(ImmutableList.<Integer>of(3, 1, 2));
		Iterator<Integer> it = heap.iterator();
		assertThat(it.hasNext(), is(true));
		assertThat(it.next(), is(equalTo(1)));
		assertThat(heap.poll(), is(equalTo(1)));
		it.remove();
		assertThat(it.hasNext(), is(true));
	}
	
	private static class Producer implements Runnable {
		private final Heap<Integer> heap;
		private final List<Integer> toPush;
		private int i = 0;
		
		public Producer(Heap<Integer> heap, List<Integer> toPush) {
			this.heap = heap;
			this.toPush = toPush;
		}
		
		@Override
		public void run() {
			while (true) {
				heap.offer(toPush.get(i++));
				if (i >= toPush.size()) {
					i = 0;
				}
			}
		}
	}
	
	private static class Consumer implements Runnable {
		private final BlockingSortedListHeap<Integer> heap;
		private final List<Integer> taken = Lists.newArrayList();
		
		public Consumer(BlockingSortedListHeap<Integer> heap) {
			this.heap = heap;
		}
		
		public List<Integer> getTaken() {
			return taken;
		}
		
		@Override
		public void run() {
			try {
				while (true) {
						taken.add(heap.take());
				}
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	private static class Remover implements Runnable {
		private final Heap<Integer> heap;
		private final List<Integer> removed = Lists.newArrayList();
		
		public Remover(Heap<Integer> heap) {
			this.heap = heap;
		}
		
		@SuppressWarnings("unused")
		public List<Integer> getRemoved() {
			return removed;
		}
		
		@Override
		public void run() {
			while (true) {
				Iterator<Integer> it = heap.iterator();
				while (it.hasNext()) {
					removed.add(it.next());
					it.remove();
				}
			}
		}
	}
	
	private static class Replacer implements Runnable {
		private final Heap<Integer> heap;
		private final List<Integer> replacements;
		private final List<Pair<Integer, Integer>> replaced = Lists.newArrayList();
		private int i = 0;
		
		public Replacer(Heap<Integer> heap, List<Integer> replacements) {
			this.heap = heap;
			this.replacements = replacements;
		}
		
		@SuppressWarnings("unused")
		public List<Pair<Integer, Integer>> getReplaced() {
			return replaced;
		}
		
		@Override
		public void run() {
			while (true) {
				HeapIterator<Integer> it = heap.iterator();
				while (it.hasNext()) {
					Integer replaceWith = replacements.get(i);
					replaced.add(Pair.of(it.next(), replaceWith));
					it.replace(replaceWith);
					++i;
					if (i >= replacements.size()) {
						i = 0;
					}
 				}
			}
		}
	}
	
	@Test
	public void testConcurrentOperations() throws InterruptedException {
		BlockingSortedListHeap<Integer> heap = (BlockingSortedListHeap<Integer>)
				factory.newHeap(ImmutableList.<Integer>of());
		ExecutorService executor = Executors.newFixedThreadPool(8);
		Consumer c1 = new Consumer(heap);
		Consumer c2 = new Consumer(heap);
		executor.submit(c1);
		executor.submit(c2);
		
		Producer p1 = new Producer(heap, ImmutableList.of(5, 9, 1, 10, 2, 2, 3, 0, 11));
		Producer p2 = new Producer(heap, ImmutableList.of(21, 22, 21, 7, 4, 25, 14, 16));
		executor.submit(p1);
		executor.submit(p2);

		Replacer l1 = new Replacer(heap, ImmutableList.of(35, 33, 30, 35, 37, 40)); 
		Replacer l2 = new Replacer(heap, ImmutableList.of(6, 8, 12, 13, 31, 39));
		executor.submit(l1);
		executor.submit(l2);
		
		Remover m1 = new Remover(heap);
		Remover m2 = new Remover(heap);
		executor.submit(m1);
		executor.submit(m2);
		
		executor.awaitTermination(10, TimeUnit.SECONDS);
		executor.shutdownNow();
		
		// The consumers should be in sorted order
		assertThat(c1.getTaken(), is(equalTo(Collections3.sort(c1.getTaken()))));
		assertThat(c2.getTaken(), is(equalTo(Collections3.sort(c2.getTaken()))));
	}
	
	// TODO(rah67): add unit tests to check that pushThenPop and takeThenPut are performed atomically.
}