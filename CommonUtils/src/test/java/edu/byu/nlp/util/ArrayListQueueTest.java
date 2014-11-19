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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.NoSuchElementException;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author rah67
 *
 */
public class ArrayListQueueTest {

	@Test
	public void testIsEmpty() {
		ArrayListQueue<Integer> deque = new ArrayListQueue<Integer>(new Object[0], 0, 0);
		assertTrue(deque.isEmpty());

		deque = new ArrayListQueue<Integer>(new Object[1], 0, 1);
		assertFalse(deque.isEmpty());
		
		deque = new ArrayListQueue<Integer>();
		assertTrue(deque.isEmpty());
		deque.offer(1);
		assertFalse(deque.isEmpty());
		deque.offer(2);
		assertFalse(deque.isEmpty());
		deque.poll();
		assertFalse(deque.isEmpty());
		deque.offer(1);
		assertFalse(deque.isEmpty());
		deque.poll();
		assertFalse(deque.isEmpty());
		deque.poll();
		assertTrue(deque.isEmpty());
	}
	
	private void fillArray(Object[] arr, int start) {
		for (int i = 0; i < arr.length; i++) {
			arr[(start + i) % arr.length] = i;
		}
	}
	
	@Test
	public void testGet() {
		final int arraySize = 23;
		Object[] arr = new Object[arraySize];
		for (int start = 0; start < arraySize; start++) {
			fillArray(arr, start);
			for (int dequeSize = 0; dequeSize < arraySize; dequeSize++) {
				ArrayListQueue<Integer> deque = new ArrayListQueue<Integer>(arr, start, dequeSize);
				for (int i = 0; i < dequeSize; i++) {
					assertEquals(i, deque.get(i).intValue());
				}
				for (int i = dequeSize; i < arraySize + 1; i++) {
					try {
						deque.get(i);
						fail();
					} catch (IndexOutOfBoundsException expected) { }
				}
			}
		}
	}

	@Test
	public void testEnsureCapacity() {
		final int arraySize = 23;
		final int maxCapacity = arraySize * 11;
		boolean capacityChanged = false;
		
		Object[] arr = new Object[arraySize];
		for (int start = 0; start < arraySize; start++) {
			fillArray(arr, start);
			for (int dequeSize = 0; dequeSize < arraySize; dequeSize++) {
				ArrayListQueue<Integer> deque = new ArrayListQueue<Integer>(arr, start, dequeSize);
				
				// Size shouldn't change!
				Assert.assertEquals(dequeSize, deque.size());
				
				// Actual capacity should be at least as large as the requested capacity.
				for (int capacity = 0; capacity < maxCapacity; capacity += 10) {
					deque.ensureCapacity(capacity);
					assertTrue(capacity <= deque.getCapacity());
					capacityChanged |= deque.getCapacity() == arr.length;
				}
				
				// Check that the data is all still available
				ArrayListQueue<Integer> expected = new ArrayListQueue<Integer>(arr, start, dequeSize);
				assert expected.size() == deque.size();
				for (int i = 0; i < expected.size(); i++) {
					assertEquals(expected.get(i), deque.get(i));
				}
			}
		}
		assert capacityChanged : "The test did not exercise the case where the capacity actually changed";
	}
	
	@Test
	public void testOffer() {
		final int arraySize = 11;
		final int numToAdd = 23;
		boolean capacityChanged = false;
		
		Object[] arr = new Object[arraySize];
		for (int start = 0; start < arraySize; start++) {
			fillArray(arr, start);
			for (int dequeSize = 0; dequeSize < arraySize; dequeSize++) {
				ArrayListQueue<Integer> deque = new ArrayListQueue<Integer>(arr, start, dequeSize);
			
				for (int i = 0; i < numToAdd; i++) {
					assertTrue(deque.offer(deque.size()));
					assertEquals(dequeSize + i + 1, deque.size());
					capacityChanged |= deque.getCapacity() == arr.length;
				}
				
				for (int i = 0; i < dequeSize + numToAdd; i++) {
					assertEquals(i, deque.get(i).intValue());
				}
			}
		}
		assert capacityChanged : "The test did not exercise the case where the capacity actually changed";
	}
	
	@Test
	public void testRemove() {
		final int arraySize = 61;
		
		Object[] arr = new Object[arraySize];
		for (int start = 0; start < arraySize; start++) {
			fillArray(arr, start);
			for (int dequeSize = 0; dequeSize < arraySize; dequeSize++) {
				ArrayListQueue<Integer> deque = new ArrayListQueue<Integer>(arr, start, dequeSize);
				
				// Use dequeSize instead of deque.size() in case of bugs in removeLast() affecting the size.
				for (int i = 0; i < dequeSize; i++) {
					assertEquals(i, deque.remove().intValue());
					assertEquals(dequeSize - i - 1, deque.size());
				}
				try {
					deque.remove();
					fail();
				} catch (NoSuchElementException expected) { }
			}
		}
	}

	@Test
	public void testPoll() {
		final int arraySize = 61;
		
		Object[] arr = new Object[arraySize];
		for (int start = 0; start < arraySize; start++) {
			fillArray(arr, start);
			for (int dequeSize = 0; dequeSize < arraySize; dequeSize++) {
				ArrayListQueue<Integer> deque = new ArrayListQueue<Integer>(arr, start, dequeSize);
				
				// Use dequeSize instead of deque.size() in case of bugs in removeLast() affecting the size.
				for (int i = 0; i < dequeSize; i++) {
					assertEquals(i, deque.poll().intValue());
					assertEquals(dequeSize - i - 1, deque.size());
				}
				assertNull(deque.poll());
			}
		}
	}
	
	@Test
	public void testPeek() {
		final int arraySize = 61;
		
		Object[] arr = new Object[arraySize];
		for (int start = 0; start < arraySize; start++) {
			fillArray(arr, start);
			for (int dequeSize = 0; dequeSize < arraySize; dequeSize++) {
				ArrayListQueue<Integer> deque = new ArrayListQueue<Integer>(arr, start, dequeSize);
				
				// Use dequeSize instead of deque.size() in case of bugs in removeLast() affecting the size.
				for (int i = 0; i < dequeSize; i++) {
					assertEquals(i, deque.peek().intValue());
					deque.remove();
				}
				assertNull(deque.peek());
			}
		}
	}
	
	@Test
	public void testElement() {
		final int arraySize = 61;
		
		Object[] arr = new Object[arraySize];
		for (int start = 0; start < arraySize; start++) {
			fillArray(arr, start);
			for (int dequeSize = 0; dequeSize < arraySize; dequeSize++) {
				ArrayListQueue<Integer> deque = new ArrayListQueue<Integer>(arr, start, dequeSize);
				
				// Use dequeSize instead of deque.size() in case of bugs in removeLast() affecting the size.
				for (int i = 0; i < dequeSize; i++) {
					assertEquals(i, deque.element().intValue());
					deque.remove();
				}
				try {
					deque.element();
					fail();
				} catch (NoSuchElementException expected) { }
			}
		}
	}
}