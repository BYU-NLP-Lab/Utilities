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

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

/**
 * @author rah67
 *
 */
public class DenseCounter implements Counter<Integer> {

    private final int[] counts;
    
    public DenseCounter(int maxEntries) {
        this.counts = new int[maxEntries];
    }
    
    @VisibleForTesting DenseCounter(int[] counts) {
        this.counts = counts;
    }
    
    /** {@inheritDoc} */
    @Override
    public int numEntries() {
        return counts.length;
    }

    /** {@inheritDoc} */
    @Override
    public int decrementCount(Integer ele, int val) {
        Preconditions.checkNotNull(ele);
        Preconditions.checkPositionIndex(ele, counts.length);

        int ret = counts[ele];
        counts[ele] -= val;
        
        return ret;
    }

    /** {@inheritDoc} */
    @Override
    public int incrementCount(Integer ele, int val) {
        Preconditions.checkNotNull(ele);
        Preconditions.checkPositionIndex(ele, counts.length);
        
        int ret = counts[ele];
        counts[ele] += val;
        
        return ret;
    }

    /** {@inheritDoc} */
    @Override
    public int getCount(Integer ele) {
        Preconditions.checkNotNull(ele);
        Preconditions.checkPositionIndex(ele, counts.length);
        
        return counts[ele];
    }

    /** {@inheritDoc} */
    @Override
    public Integer argMax() {
        if (counts.length == 0) {
            return -1;
        }
        
        int argMax = 0;
        int max = counts[0];
        for (int i = 1; i < counts.length; i++) {
            if (counts[i] > max) {
                argMax = i;
                max = counts[i];
            }
        }
        return argMax;
    }

    /** {@inheritDoc} */
    @Override
    public Integer argMax(RandomGenerator rnd) {
        if (counts.length == 0) {
            return -1;
        }
        
        int argMax = 0;
        int max = counts[0];
        int tieCount = 0;
        for (int i = 1; i < counts.length; i++) {
            if (counts[i] > max) {
                argMax = i;
                max = counts[i];
                tieCount = 0;
            } else if (counts[i] == max) {
                // To break ties, we have to randomly select one of the ties. The following algorithm is reservoir
                // sampling with a reservoir of size 1.
                int u = rnd.nextInt(++tieCount + 1);
                if (u == 0) {
                    argMax = i;
                }
            }
        }
        return argMax;
    }
    
    private class EntryIterator implements Iterator<Entry<Integer,Integer>> {

        private int i = 0;
        
        /** {@inheritDoc} */
        @Override
        public boolean hasNext() {
            return i < counts.length;
        }

        /** {@inheritDoc} */
        @Override
        public Entry<Integer, Integer> next() {
            return new Entry<Integer, Integer>() {
                private final int key = i;
                private final int value = counts[i++];
                
                @Override
                public Integer getKey() {
                    return key;
                }

                @Override
                public Integer getValue() {
                    return value;
                }

                @Override
                public Integer setValue(Integer count) {
                    int ret = counts[key];
                    counts[key] = count;
                    return ret;
                }

                @Override
                public boolean equals(Object that) {
                    if (that == null || !(that instanceof Entry)) {
                        return false;
                    }
                    @SuppressWarnings("unchecked")
                    Entry<Integer, Integer> other = (Entry<Integer, Integer>) that;
                    return getKey() == other.getKey() && getValue() == other.getValue();
                }
                
                @Override
                public String toString() {
                    return getKey() + "=" + getValue();
                }

                @Override
                public int hashCode() {
                    final int prime = 31;
                    int result = 1;
                    result = prime * result + getKey().hashCode();
                    result = prime * result + getValue().hashCode();
                    return result;
                }
            };
        }

        /** {@inheritDoc} */
        @Override
        public void remove() {
            counts[i] = 0;
        }
        
    }
    
    private class EntrySet extends AbstractSet<Entry<Integer, Integer>> {

        /** {@inheritDoc} */
        @Override
        public Iterator<Entry<Integer, Integer>> iterator() {
            return new EntryIterator();
        }

        /** {@inheritDoc} */
        @Override
        public int size() {
            return counts.length;
        }
        
    }
    
    /** {@inheritDoc} */
    @Override
    public Set<Entry<Integer, Integer>> entrySet() {
        return new EntrySet();
    }

    /** {@inheritDoc} */
    @Override
    public int totalCount() {
        int sum = 0;
        for (int i = 0; i < counts.length; i++) {
            sum += counts[i];
        }
        return sum;
    }

    public static DenseCounter from(int numLabels, Iterable<Integer> elements) {
        int[] counts = new int[numLabels];
        for (Integer i : elements) {
            ++counts[i];
        }
        return new DenseCounter(counts);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "DenseCounter [counts=" + Arrays.toString(counts) + "]";
    }

    /** {@inheritDoc} */
    @Override
    public List<Integer> argMaxList(int topn, RandomGenerator rnd) {
      return Counters.argMaxList(entrySet(), topn, rnd);
    }
}
