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
package edu.byu.nlp.data.streams;

import com.google.common.base.Function;

import edu.byu.nlp.util.Indexer;

/**
 * @author rah67
 *
 */
public class FieldIndexer<E> implements Function<E,Integer> {
	
	private final Indexer<E> indexer;
	
	public FieldIndexer(Indexer<E> indexer) {
		this.indexer = indexer;
	}
	
	/** {@inheritDoc} */
	@Override
	public Integer apply(E ele) {
		return indexer.indexOf(ele);
	}

	/**
	 * @author plf1
	 * 
	 * Casts Integer indices to longs instead. 
	 */
	public static <E> Function<E,Long> cast2Long(final Function<E,Integer> indexer) {
		return new Function<E, Long>() {
			@Override
			public Long apply(E input) {
				return (long)(int) indexer.apply(input);
			}
		};
	}
}
