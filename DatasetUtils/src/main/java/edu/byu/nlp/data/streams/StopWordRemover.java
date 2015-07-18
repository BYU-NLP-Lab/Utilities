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

import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.Sets;

import edu.byu.nlp.io.Files2;

/**
 * @author rah67
 *
 */
public class StopWordRemover implements Function<String, String> {

	private static final String MALLET_STOP_WORDS_FILE = "/mallet_stopwords.txt";
	private static final String TWITTER_STOP_WORDS_FILE = "/twitter_stopwords.txt";
	
  private Set<String> stopWords;
	
	public StopWordRemover(Set<String> stopWords) {
		this.stopWords = stopWords;
	}
	
	/** {@inheritDoc} */
	@Override
	public String apply(String word) {
	  if (word==null){
	    return null;
	  }
	  
	  return (stopWords.contains(word.toLowerCase()))? null: word;
	}

	public static Set<String> malletStopWords(){
		return Sets.newHashSet(Files2.open(StopWordRemover.class, MALLET_STOP_WORDS_FILE));
	}

	public static Set<String> twitterStopWords(){
		return Sets.newHashSet(Files2.open(StopWordRemover.class, TWITTER_STOP_WORDS_FILE));
	}
	
	public static StopWordRemover fromWords(Set<String> words) {
		return new StopWordRemover(words);
	}
	
	public static StopWordRemover malletStopWordRemover() {
		return fromWords(malletStopWords());
	}

	public static StopWordRemover twitterStopWordRemover() {
		return fromWords(twitterStopWords());
	}
	
}
