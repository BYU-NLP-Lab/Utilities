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

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

/**
 * @author rah67
 *
 */
public class Indexer<E> implements Iterable<E> {
	
	private final List<E> list; 
	private final Object2IntOpenHashMap<E> map;
	
	public Indexer() {
		list = Lists.newArrayList();
		map = new Object2IntOpenHashMap<E>();
		map.defaultReturnValue(-1);
	}
	
	public int add(E ele) {
		int index = map.getInt(ele);
		if (index == -1) { // Does not exist yet; add it
			index = map.size();
			map.put(ele, index);
			list.add(ele);
		}
		return index;
	}
	
	public int indexOf(E ele) {
		return map.getInt(ele);
	}

	public E get(int index) {
		return list.get(index);
	}
	
	public boolean contains(Object ele) {
		return map.containsKey(ele);
	}
	
	public int size() {
		return list.size();
	}

	public Indexer<E> retain(BitSet featureSet) {
		Indexer<E> newIndexer = new Indexer<E>();

		// TODO: can I use nextSetBit()??
		for (int i = 0; i < featureSet.length(); i++) {
			if (featureSet.get(i)) {
				newIndexer.add(this.get(i));
			}
		}

		return newIndexer;
	}
	
	@Override
	public String toString() {
		return list.toString();
	}

	/** {@inheritDoc} */
	@Override
	public Iterator<E> iterator() {
		return Iterators.unmodifiableIterator(list.iterator());
	}

}
