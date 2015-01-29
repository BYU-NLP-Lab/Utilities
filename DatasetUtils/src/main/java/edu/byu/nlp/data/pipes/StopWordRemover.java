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
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.byu.nlp.io.Files2;

/**
 * @author rah67
 *
 */
public class StopWordRemover implements Function<List<String>, List<String>> {

	private static final String DEFAULT_STOP_WORDS_FILE = "/mallet_stopwords.txt";
	
	private class StopWordPredicate implements Predicate<String> {

		private final Set<String> stopWords;

		public StopWordPredicate(Set<String> stopWords) {
			this.stopWords = stopWords;
		}

		/** {@inheritDoc} */
		@Override
		public boolean apply(String word) {
			return !stopWords.contains(word.toLowerCase());
		}
		
	}

	private final StopWordPredicate p;
	
	public StopWordRemover(Set<String> stopWords) {
		this.p = new StopWordPredicate(stopWords);
	}
	
	/** {@inheritDoc} */
	@Override
	public List<String> apply(List<String> input) {
		return Lists.newArrayList(Iterables.filter(input, p));
	}
	
	public static StopWordRemover malletStopWords() {
		return new StopWordRemover(Sets.newHashSet(Files2.open(StopWordRemover.class, DEFAULT_STOP_WORDS_FILE)));
	}
	
}
