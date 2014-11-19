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
 * Factory methods for common caches.
 * 
 * @author rah67
 *
 */
@Deprecated
public class Caches {

	// Uninstantiable
	private Caches() {}
	
	/**
	 * Caches the last unique value requested (uses == for equality test).
	 * 
	 * @author rah67
	 *
	 * @param <K> type of key
	 * @param <V> type of value cached
	 */
	private static class LastValueReferenceCacheStorage<K, V> implements CacheStorage<K, V> {
		
		private K cachedKey;
		private V cachedValue;
		
		@Override
		public V getOrReturnNull(K key) {
			if (key == cachedKey) {
				return cachedValue;
			}
			return null;
		}

		@Override
		public void updateCache(K key, V value) {
			this.cachedKey = key;
			this.cachedValue = value;
		}
	}
	
	/**
	 * Creates a cache which caches the last unique value requested (uses == for equality test).
	 */
	public static <K, V> Cache<K, V> lastValueReferenceCache(ValueSupplier<K, V> valueSupplier) {
		return new Cache<K, V>(new LastValueReferenceCacheStorage<K, V>(), valueSupplier);
	}
}
