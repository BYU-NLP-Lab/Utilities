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

import java.util.AbstractList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;


/**
 * An array-backed Queue that grows dynamically. Unlike ArrayDeque, this class has a get(i) method.
 * 
 * @author rah67
 *
 */
public class ArrayListQueue<E> extends AbstractList<E> implements List<E>, Queue<E> {

	private static final int DEFAULT_INITIAL_SIZE = 10;
	
	private Object[] values;
	private int start;	// inclusive
	private int size;
	private int modCount;
	
	public ArrayListQueue() {
		this(DEFAULT_INITIAL_SIZE);
	}
	
	public ArrayListQueue(int capacity) {
		this(new Object[capacity], 0, 0);
	}
	
	@VisibleForTesting ArrayListQueue(Object[] values, int start, int size) {
		assert values != null;
		assert start >= 0 && start < values.length;
		assert size <= values.length;
		
		this.values = values;
		this.start = start;
		this.size = size;
		this.modCount = 0;
	}
	
	@Override
	public boolean isEmpty() {
		return size == 0;
	}
	
	@Override
	public int size() {
		return size;
	}
	
	private int actualIndex(int i) {
		int actualIndex = start + i;
		if (actualIndex >= values.length) {
			actualIndex -= values.length;
		}
		return actualIndex;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public E get(int i) {
		Preconditions.checkElementIndex(i, size);

		return (E) values[actualIndex(i)];
	}
	
	// FIXME(rah67): not overflow aware!
	public void ensureCapacity(int minCapacity) {
		if (minCapacity < size) {
			return;
		}
		
		int oldCapacity = values.length;
		int newCapacity = oldCapacity + (oldCapacity >> 1);
		if (newCapacity < minCapacity) {
			newCapacity = minCapacity;
		}
		Object[] newValues = new Object[newCapacity];
		if (start + size < values.length) {
			System.arraycopy(values, start, newValues, 0, size);
		} else {
			System.arraycopy(values, start, newValues, 0, values.length - start);
			System.arraycopy(values, 0, newValues, values.length - start, size - (values.length - start));
		}
		values = newValues;
		start = 0;
		++modCount;
	}
	
	@VisibleForTesting int getCapacity() {
		return values.length;
	}
	
	@Override
	public boolean add(E e) {
		return offer(e);
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean offer(E e) {
		ensureCapacity(size + 1);
		values[actualIndex(size)] = e;
		++size;
		++modCount;
		return true;
	}
	
	/** {@inheritDoc} */
	@Override
	public E peek() {
		if (size == 0) {
			return null;
		}
		return get(0);
	}

	/** {@inheritDoc} */
	@Override
	public E element() {
		if (size == 0) {
			throw new NoSuchElementException();
		}
		return get(0);
	}

	// Shared by remove() and poll().
	public E uncheckedRemoveFirst() {
		E first = get(0);
		if (++start >= values.length) {
			start = 0;
		}
		--size;
		++modCount;
		return first;
	}
	
	/** {@inheritDoc} */
	@Override
	public E poll() {
		if (size == 0) {
			return null;
		}
		return uncheckedRemoveFirst();
	}

	/** {@inheritDoc} */
	@Override
	public E remove() {
		if (isEmpty()) {
			throw new NoSuchElementException();
		}
		return uncheckedRemoveFirst();
	}
}