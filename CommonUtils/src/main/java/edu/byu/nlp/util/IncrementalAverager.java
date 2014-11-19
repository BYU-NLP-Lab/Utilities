/**
 * Copyright 2013 Brigham Young University
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


/**
 * Incrementally computes an average for multiple independent items.
 */
public class IncrementalAverager<E> {
    private final Vector<E> avg = new HashVector<E>();
    private final Counter<E> counts = new HashCounter<E>();
    
    /**
     * Adds the value to the running average.
     */
    public void addValue(E ele, double val) {
        double count = counts.getCount(ele);
        double ratio = count / (count + 1.0);
        double newAvg = avg.getValue(ele) * ratio + val / (count + 1.0);
        avg.setValue(ele, newAvg);
        counts.incrementCount(ele, 1);
    }
    
    public double average(E ele) {
        return avg.getValue(ele);
    }
    
    /**
     * The number of times the elements was seen 
     */
    public int count(E ele) {
        return counts.getCount(ele);
    }
}