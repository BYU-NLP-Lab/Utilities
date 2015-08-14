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

import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.math3.random.RandomGenerator;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

/**
 * @author rah67
 *
 */
public abstract class AbstractCounter<E> implements Counter<E> {

    /** {@inheritDoc} */
    @Override
    public int numEntries() {
        return entrySet().size();
    }

    /** {@inheritDoc} */
    @Override
    public List<E> argMaxList(int topn, RandomGenerator rnd) {
      return Counters.argMaxList(entrySet(), topn, rnd);
    }
    
    /** {@inheritDoc} */
    @Override
    public E argMax() {
        E argMax = null;
        int maxCount = Integer.MIN_VALUE;
        for (Entry<E, Integer> entry : entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                argMax = entry.getKey();
            }
        }
        return argMax;
    }
    
    /** {@inheritDoc} */
    @Override
    public E argMax(RandomGenerator rnd) {
        E argMax = null;
        int maxCount = Integer.MIN_VALUE;
        int tieCount = 0;
        for (Entry<E, Integer> entry : entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                argMax = entry.getKey();
                tieCount = 0;
            } else if (entry.getValue() == maxCount) {
                // To break ties, we have to randomly select one of the ties. The following algorithm is reservoir
                // sampling with a reservoir of size 1.
                int u = rnd.nextInt(++tieCount + 1);
                if (u == 0) {
                    argMax = entry.getKey();
                }
            }
        }
        return argMax;
    }
    
    @Override
    public int totalCount() {
        int sum = 0;
        for (Entry<E, Integer> entry : entrySet()) {
            sum += entry.getValue();
        }
        return sum;
    }
    
    @Override
    public String toString() {
      StringBuilder b = new StringBuilder();
      b.append("Counter {");
      
      List<String> parts = Lists.newArrayList();
      for (Entry<E, Integer> e: entrySet()){
        parts.add(e.getKey()+"="+e.getValue());
      }
      Joiner.on(", ").appendTo(b, parts);
      b.append("}");
        
      return b.toString();
    }
}
