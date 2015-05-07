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

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.deeplearning4j.models.word2vec.CustomWord2Vec;
import org.deeplearning4j.text.sentenceiterator.BaseSentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.deeplearning4j.util.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import edu.byu.nlp.data.FlatInstance;
import edu.byu.nlp.data.pipes.DataSource;
import edu.byu.nlp.data.types.SparseFeatureVector;
import edu.byu.nlp.dataset.BasicSparseFeatureVector;
import edu.byu.nlp.util.DoubleArrays;
import edu.byu.nlp.util.IntArrays;

/**
 * @author plf1
 *
 * An alternative way of extracting features from documents. Takes 
 * the place of CountVectorizer and Feature Selectors
 * 
 * http://deeplearning4j.org/word2vec.html
 *
 */
public class Word2VecCountVectorizer implements Function<List<List<String>>,SparseFeatureVector> {
	
	private static Logger logger = LoggerFactory.getLogger(Word2VecCountVectorizer.class);

	public static final String CACHE_DIRNAME = "word2vec";
	public static final String CACHE_FILENAME = "word2vec.dat";
	public static final String INDEX_DIRNAME = "word2vec-index";
	
	///////////////////////////////
	// Builder
	///////////////////////////////
	public static Word2VecCountVectorizer build(DataSource<List<List<String>>, String> src) throws IOException {
		
		// cache word vectors per dataset+size 
		File cacheDir = cacheDir(src);
		File cacheFile = new File(cacheDir, CACHE_FILENAME);
		
		// get a word2vec instance
		CustomWord2Vec word2vec;
		if (cacheFile!=null && cacheFile.exists()){
			logger.info("loading word2vec instance from "+cacheFile);
			// load word2vec from file
			word2vec = SerializationUtils.readObject(cacheFile);
      logger.info("done loading");
		}
		else{
			logger.info("fitting new word2vec instance");
			// create new word2vec instance
			word2vec = buildWord2Vec(src,cacheDir);
			
			if (cacheFile!=null){
				logger.info("saving word2vec instance to "+cacheFile);
				// cache word2vec for future use
				Files.createParentDirs(cacheFile);
				SerializationUtils.saveObject(word2vec, cacheFile);
        logger.info("done saving");
			}
		}
		
		// find the min value in any word vector (so we can adjust for it later)
		return new Word2VecCountVectorizer(word2vec);
	}

	private static File cacheDir(DataSource<List<List<String>>, String> src){
		String sanitizedSource = src.getSource().replace('/', '-');
		int numInstances = Lists.newArrayList(src.getLabeledInstances()).size();
		return new File(CACHE_DIRNAME,sanitizedSource+"-"+numInstances);
	}
	

	///////////////////////////////
	// Main Class
	///////////////////////////////
	
	private CustomWord2Vec word2vec;
	
	public Word2VecCountVectorizer(CustomWord2Vec word2vec) {
		this.word2vec=word2vec;
	}

	@Override
	public SparseFeatureVector apply(List<List<String>> doc) {

		 double[] documentVector = new double[word2vec.lookupTable().layerSize()];
		 for (List<String> sentence: doc){
			 for (String word: sentence){
				 double[] wordvec = word2vec.getWordVector(word);
//				 word2vec.wordsNearest("king", 5);
//				 word2vec.wordsNearest("computer", 5);
				 DoubleArrays.addToSelf(documentVector, wordvec);
			 }
		 }
		 
		 // return a dense feature vector
		 return new BasicSparseFeatureVector(
				 IntArrays.sequence(0, documentVector.length),
				 documentVector);
	}

	 public int getWordVectorSize(){
		 return word2vec.lookupTable().layerSize();
	 }
	

	///////////////////////////////
	// Helper Code
	///////////////////////////////
	/**
	 * Iterates over the sentences in a dataset. 
	 * This class joins preprocessed token simply (a single space) making tokenization trivial 
	 * (the DefaultTokenizerFactory delegates to java.util.StringTokenizer, 
	 * which splits on whitespace. Therefore, the tokens that word2vec receives should
	 * be the same as those in the original dataset).
	 */
	private static class SimpleSentenceIterator extends BaseSentenceIterator {
		private List<String> sentences;
		private Iterator<String> sentenceIterator;

		public SimpleSentenceIterator(Iterable<FlatInstance<List<List<String>>, String>> instances) {

			this.sentences = Lists.newArrayList();
			for (FlatInstance<List<List<String>>,String> inst : instances) {
				for (List<String> sent: inst.getData()){
				  if (sent.size()>1){
				    sentences.add(Joiner.on(" ").join(sent));
				  }
				}
			}
			
			reset();
		}
		@Override
		public void reset() {
			sentenceIterator = sentences.iterator();
		}
		@Override
		public String nextSentence() {
			return sentenceIterator.next();
		}
		@Override
		public boolean hasNext() {
			return sentenceIterator.hasNext();
		}
	}

	private static CustomWord2Vec buildWord2Vec(DataSource<List<List<String>>, String> src, File cacheDir) throws IOException{

		Iterable<FlatInstance<List<List<String>>, String>> data = src.getLabeledInstances();
		CustomWord2Vec word2vec = new CustomWord2Vec.Builder()
			.iterate(new SimpleSentenceIterator(data))
//			.useAdaGrad(true) // when active, word2vec produces nonsense (cosine angle almost 0 among all words)
			.tokenizerFactory(new DefaultTokenizerFactory())
			.indexDirectory(new File(cacheDir,INDEX_DIRNAME))
			// custom
      .layerSize(300) // how big are word vectors
//      .learningRate(1e-3)
      .minWordFrequency(5)
			// defaults (shown here for reference)
//			.iterations(1)
//			.windowSize(5)
//			.layerSize(50) // how big are word vectors
//			.vocabCache(new InMemoryLookupCache())
//			.learningRate(2.5e-1)
			.build();
		
		word2vec.fit();
		
		return word2vec;
	}

	
	
	
	
}
