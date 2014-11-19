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

import java.util.List;

import edu.byu.nlp.data.FlatInstance;
import edu.byu.nlp.util.Counter;
import edu.byu.nlp.util.HashCounter;

/**
 * @author rah67
 * @author plf1
 *
 */
public class WordCounter<W, L> implements DataSink<List<W>, L, Counter<W>> {

	@Override
	public Counter<W> processLabeledInstances(Iterable<FlatInstance<List<W>, L>> data) {
		Counter<W> counter = new HashCounter<W>();
		for (FlatInstance<List<W>, L> label : data) {
			for (W word : label.getData()) {
				counter.incrementCount(word, 1);
			}
		}
		return counter;
	}

}
