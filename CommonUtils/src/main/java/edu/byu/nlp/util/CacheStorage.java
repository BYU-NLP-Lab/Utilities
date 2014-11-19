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
 * The actual storage for a cache.
 * 
 * @author rah67
 *
 * @param <K> type of key stored
 * @param <V> type of value cached
*/
@Deprecated
public interface CacheStorage<K, V> {
	/**
	 * Returns the value associated with key if it exists; otherwise, returns null.
	 */
	V getOrReturnNull(K key);
	
	/**
	 * Upon a cache miss, updates the cache's association of key to the new value.
	 */
	void updateCache(K key, V value);
}
