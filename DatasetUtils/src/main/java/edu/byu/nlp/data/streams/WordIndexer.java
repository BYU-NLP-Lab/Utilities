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

import java.util.List;
import java.util.Map;

import edu.byu.nlp.data.types.DataStreamInstance;
import edu.byu.nlp.util.Indexer;

/**
 * @author rah67
 *
 */
public class WordIndexer<W, L> implements DataStreamSink<Indexer<W>> {

	@SuppressWarnings("unchecked")
  @Override
  public Indexer<W> process(Iterable<Map<String, Object>> data) {
		Indexer<W> indexer = new Indexer<W>();
		for (Map<String,Object> instance : data) {
			for (W word : (List<W>)DataStreamInstance.getData(instance)) {
				indexer.add(word);
			}
		}
		return indexer;
	}

}
