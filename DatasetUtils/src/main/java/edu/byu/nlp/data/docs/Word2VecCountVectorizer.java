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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.List;

import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.BaseSentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.deeplearning4j.util.SerializationUtils;
import org.nd4j.linalg.api.ndarray.INDArray;
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
public class Word2VecCountVectorizer implements Function<List<String>,SparseFeatureVector> {
	
	private static Logger logger = LoggerFactory.getLogger(Word2VecCountVectorizer.class);

	public static String TMP_DIR = "/tmp/datasetfeatures/";
	public static String CACHE_FILENAME = "word2vec.dat";
	public static String INDEX_DIRNAME = "word2vec-index";
	
	///////////////////////////////
	// Builder
	///////////////////////////////
	public static Word2VecCountVectorizer build(DataSource<List<String>, String> src) throws IOException {
		
		// cache word vectors per dataset+size 
		File cacheDir = cacheDir(src);
		File cacheFile = new File(cacheDir, CACHE_FILENAME);
		
		// get a word2vec instance
		Word2Vec word2vec;
		if (cacheFile!=null && cacheFile.exists()){
			logger.info("loading word2vec instance from "+cacheFile);
			// load word2vec from file
			word2vec = SerializationUtils.readObject(cacheFile);
		}
		else{
			logger.info("fitting new word2vec instance");
			// create new word2vec instance
			word2vec = buildWord2vec(src,cacheDir);
			
			if (cacheFile!=null){
				logger.info("saving word2vec instance to "+cacheFile);
				// cache word2vec for future use
				Files.createParentDirs(cacheFile);
				SerializationUtils.saveObject(word2vec, cacheFile);
			}
		}
		
		// find the min value in any word vector (so we can adjust for it later)
		double min = minWordVecEntry(word2vec);
		
		return new Word2VecCountVectorizer(word2vec, min);
	}

	private static File cacheDir(DataSource<List<String>, String> src){
		String sanitizedSource = src.getSource().replace("file://", "").replace('/', '-');
		int numInstances = Lists.newArrayList(src.getLabeledInstances()).size();
		return new File(TMP_DIR+sanitizedSource+"-"+numInstances);
	}
	

	///////////////////////////////
	// Main Class
	///////////////////////////////
	
	private Word2Vec word2vec;
	private double wordVectorOffset;
	
	public Word2VecCountVectorizer(Word2Vec word2vec, double minWord2vecWeight) {
		this.word2vec=word2vec;
		this.wordVectorOffset=Math.max(0,-minWord2vecWeight);
	}

	 @Override
	public SparseFeatureVector apply(List<String> input) {

		 double[] documentVector = new double[word2vec.lookupTable().layerSize()];
		 for (String word: input){
			 double[] wordvec = word2vec.getWordVector(word);
			 DoubleArrays.addToSelf(wordvec, wordVectorOffset); // ensure doc features are positive
			 DoubleArrays.addToSelf(documentVector, wordvec);
		 }
		 // return a dense feature vector
		 return new BasicSparseFeatureVector(
				 IntArrays.sequence(0, documentVector.length),
				 documentVector);
	}

	

	///////////////////////////////
	// Helper Code
	///////////////////////////////
	/**
	 * Iterates over the sentences in a dataset. This class joins preprocessed
	 * token simply (a single space) making tokenization trivial (the
	 * DefaultTokenizerFactory delegates to java.util.StringTokenizer, which
	 * splits on whitespace. Therefore, the tokens that word2vec receives should
	 * be the same as those in the original dataset).
	 */
	private static class SimpleSentenceIterator extends BaseSentenceIterator {
		private List<String> sentences;
		private Iterator<String> sentenceIterator;

		public SimpleSentenceIterator(Iterable<FlatInstance<List<String>, String>> instances) {
			// collect sentences for easy iteration
			this.sentences = Lists.newArrayList();
			for (FlatInstance<List<String>, String> inst : instances) {
				sentences.add(Joiner.on(" ").join(inst.getData()));
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

	private static double minWordVecEntry(Word2Vec word2vec) {
		double globalMin = Double.POSITIVE_INFINITY;
		 for (Iterator<INDArray> itr = word2vec.lookupTable().vectors(); itr.hasNext();){
			 double localMin = itr.next().ravel().min(0).getDouble(0);
			 globalMin = Math.min(globalMin, localMin);
		 }
		 return globalMin;
	}

	private static Word2Vec buildWord2vec(DataSource<List<String>, String> src, File cacheDir) throws IOException{

		Iterable<FlatInstance<List<String>, String>> data = src.getLabeledInstances();
		TokenizerFactory tokenizerFactory = new DefaultTokenizerFactory(); // see javadoc for SimpleSentenceIterator
		Word2Vec word2vec = new Word2Vec.Builder()
			.iterate(new SimpleSentenceIterator(data))
			.useAdaGrad(true)
			.tokenizerFactory(tokenizerFactory)
			.indexDirectory(new File(cacheDir,INDEX_DIRNAME))
//			.setIndexDirectory(null) // this method SHOULD, but doesn't, exit. So we use the hack above
			// use defaults for most things (shown here for reference)
//			.windowSize(5)
//			.layerSize(50) // how big are word vectors
//			.vocabCache(new InMemoryLookupCache())
//			.learningRate(2.5e-1)
			.build();
		
		word2vec.fit();
		
		return word2vec;
	}
	
	
	
	
	
	
}
