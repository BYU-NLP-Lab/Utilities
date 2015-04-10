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

import java.util.List;

import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.models.word2vec.wordstore.inmemory.InMemoryLookupCache;
import org.deeplearning4j.text.sentenceiterator.BaseSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import edu.byu.nlp.data.FlatInstance;
import edu.byu.nlp.data.pipes.DataSource;
import edu.byu.nlp.data.types.SparseFeatureVector;
import edu.byu.nlp.dataset.BasicSparseFeatureVector;

/**
 * @author plf1
 *
 */
public class Word2VecCountVectorizer implements Function<List<String>, SparseFeatureVector> {
	
	public static Word2VecCountVectorizer build(DataSource<List<String>, String> src) {
		
		
		TokenizerFactory factory;
		Word2Vec vec = new Word2Vec.Builder().iterate(iter).tokenizerFactory(factory)
			    .learningRate(1e-3).vocabCache(new InMemoryLookupCache())
			    .layerSize(300).windowSize(5).build();
			vec.fit();
			
			new Word2Vec.Builder().iter
//		Word2Vec vec = new Word2Vec.Builder().windowSize(5).layerSize(300).iterate(iter).tokenizerFactory(t).build();
		
		return null;
	}
	
	@Override
	public SparseFeatureVector apply(List<String> input) {
		INDArray wordVector = vec.getWordVectorMatrix("myword");
	    double[] wordVector = vec.getWordVector("myword");
		
		return new BasicSparseFeatureVector(features.toIntArray(), counts.toDoubleArray());
	}
	
	
	private class FlatInstanceSentenceIterator extends BaseSentenceIterator{
		private List<String> sentences;
		public FlatInstanceSentenceIterator(Iterable<FlatInstance<List<String>, String>> instances){

			// collect sentences for easy iteration
			this.sentences = Lists.newArrayList();
			for (FlatInstance<List<String>, String> inst: instances){
				sentences.add(Joiner.on(" ").join(inst.getData()));
			}
			
		}
		@Override
		public void reset() {
		}
		
		@Override
		public String nextSentence() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public boolean hasNext() {
			// TODO Auto-generated method stub
			return false;
		}
	}

}
