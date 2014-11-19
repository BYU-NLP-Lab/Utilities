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

import java.util.Map.Entry;
import java.util.Set;

/**
 * @author rah67
 *
 */
public class HashCounter<E> extends AbstractCounter<E> implements Counter<E> {

	private final Object2IntOpenHashMap<E> map;
	
	public HashCounter() {
		this.map = new Object2IntOpenHashMap<E>();
	}
	
	/** {@inheritDoc} */
	@Override
	public int numEntries() {
		return map.size();
	}
	
	/** {@inheritDoc} */
	@Override
	public int incrementCount(E ele, int val) {
		int newCount = map.getInt(ele) + val;
		if (newCount == 0) {
			return map.removeInt(ele);
		}
		return map.put(ele, newCount);
	}
	
	/** {@inheritDoc} */
	@Override
	public int decrementCount(E ele, int val) {
		int newCount = map.getInt(ele) - val;
		if (newCount == 0) {
			return map.removeInt(ele);
		}
		return map.put(ele, newCount);
	}
	
	/** {@inheritDoc} */
	@Override
	public int getCount(E ele) {
		return map.getInt(ele);
	}
	
	/** {@inheritDoc} */
	@Override
	public Set<Entry<E, Integer>> entrySet() {
		return map.entrySet();
	}

}
