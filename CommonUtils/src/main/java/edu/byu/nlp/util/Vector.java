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

import java.util.Map.Entry;
import java.util.Set;

/**
 * @author rah67
 *
 */
public interface Vector<E> {
    boolean contains(E ele);
    
    int numEntries();
    
    double add(E ele, double val);
    double divide(E ele, double val);
    
    double getValue(E ele);
    double setValue(E ele, double val);
    
    Set<Entry<E, Double>> entrySet();
}
