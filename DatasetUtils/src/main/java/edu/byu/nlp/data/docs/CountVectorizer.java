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
package edu.byu.nlp.data.docs;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.util.List;
import java.util.Map.Entry;

import com.google.common.base.Function;

import edu.byu.nlp.data.types.SparseFeatureVector;
import edu.byu.nlp.dataset.BasicSparseFeatureVector;
import edu.byu.nlp.util.Counters;
import edu.byu.nlp.util.Indexer;

/**
 * @author rah67
 *
 */
public class CountVectorizer<E> implements Function<List<E>, SparseFeatureVector> {
	
	private final Indexer<E> indexer;
	
	public CountVectorizer(Indexer<E> indexer) {
		this.indexer = indexer;
	}
	
	/** {@inheritDoc} */
	@Override
	public SparseFeatureVector apply(List<E> words) {
		IntArrayList features = new IntArrayList();
		DoubleArrayList counts = new DoubleArrayList();
		for (Entry<E, Integer> wordAndCount : Counters.count(words).entrySet()) {
			int index = indexer.indexOf(wordAndCount.getKey());
			// Skip words that weren't seen
			if (index >= 0) {
				features.add(index);
				counts.add(wordAndCount.getValue());
			}
		}
		
		return new BasicSparseFeatureVector(features.toIntArray(), counts.toDoubleArray());
	}

}
