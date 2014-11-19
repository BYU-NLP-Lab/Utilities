/**
 * Copyright 2011 Brigham Young University
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
 * Supplies a value for the given key. Similar to a map or function, but without the ability to store values.
 * 
 * @author rah67
 *
 * @param <K> the type of key for which values are supplied
 * @param <V> the type of values that are supplied
 */
public interface ValueSupplier<K, V> {
	/**
	 * Returns the value associated with the provided key.
	 */
	V get(K key);
}