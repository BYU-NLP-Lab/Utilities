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

import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;

import java.util.Map.Entry;
import java.util.Set;

/**
 * @author rah67
 *
 */
public class HashVector<E> implements Vector<E> {

	private final Object2DoubleOpenHashMap<E> map;
	
	public HashVector() {
	    this(0.0);
	}
	
    public HashVector(double defaultValue) {
        this.map = new Object2DoubleOpenHashMap<E>();
        this.map.defaultReturnValue(defaultValue);
    }
    
	/** {@inheritDoc} */
	@Override
	public int numEntries() {
		return map.size();
	}
	
	/** {@inheritDoc} */
	@Override
	public double add(E ele, double val) {
		double newCount = map.getDouble(ele) + val;
		if (newCount == 0.0) {
			return map.removeDouble(ele);
		}
		return map.put(ele, newCount);
	}
	
	@Override
    public double divide(E ele, double val) {
        double newCount = map.getDouble(ele) / val;
        if (newCount == 0.0) {
            return map.removeDouble(ele);
        }
        return map.put(ele, newCount);
    }
    
	/** {@inheritDoc} */
	@Override
	public double getValue(E ele) {
		return map.getDouble(ele);
	}
	
	/** {@inheritDoc} */
	@Override
	public Set<Entry<E, Double>> entrySet() {
		return map.entrySet();
	}

    /** {@inheritDoc} */
    @Override
    public boolean contains(E ele) {
        return map.containsKey(ele);
    }

    /** {@inheritDoc} */
    @Override
    public double setValue(E ele, double val) {
        return map.put(ele, val);
    }

}
