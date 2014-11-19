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
 * Caches the last unique value requested (uses == for equality test). Follows the decorator pattern.
 * 
 * @author rah67
 *
 * @param <K> type of key maintained by this cache
 * @param <V> type of value cached
 */
@Deprecated
public class Cache<K, V> implements ValueSupplier<K, V> {
	
	private final CacheStorage<K, V> cacheStorage;
	private final ValueSupplier<K, V> valueSupplier;
	
	public Cache(CacheStorage<K, V> cacheStorage, ValueSupplier<K, V> valueSupplier) {
		this.cacheStorage = cacheStorage;
		this.valueSupplier = valueSupplier;
	}

	public V get(K key) {
		V value = cacheStorage.getOrReturnNull(key);
		if (value == null) {
			value = valueSupplier.get(key);
			cacheStorage.updateCache(key, value);
		}
		return value;
	}
}