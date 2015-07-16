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
package edu.byu.nlp.data.pipes;

import edu.byu.nlp.data.FlatInstance;
import edu.byu.nlp.util.Indexer;

/**
 * @author rah67
 * @author plf1
 *
 */
public class LabelIndexer<D, L> implements DataSink<D, L, Indexer<L>> {

	@Override
	public Indexer<L> processLabeledInstances(Iterable<FlatInstance<D, L>> data) {
		Indexer<L> indexer = new Indexer<L>();
		for (FlatInstance<D, L> instance : data) {
			indexer.add(instance.getLabel());
		}
		return indexer;
	}

}
