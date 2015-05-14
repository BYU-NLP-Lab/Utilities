/**
 * Copyright 2013 Brigham Young University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.byu.nlp.data.docs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

import org.apache.commons.vfs2.FileObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Lists;

import edu.byu.nlp.data.FlatInstance;
import edu.byu.nlp.data.pipes.DataSource;
import edu.byu.nlp.data.pipes.DataSources;
import edu.byu.nlp.data.pipes.Downcase;
import edu.byu.nlp.data.pipes.FieldIndexer;
import edu.byu.nlp.data.pipes.FilenameToContents;
import edu.byu.nlp.data.pipes.IndexFileToLabeledFileList;
import edu.byu.nlp.data.pipes.IndexerCalculator;
import edu.byu.nlp.data.pipes.JSONFileToAnnotatedDocumentList;
import edu.byu.nlp.data.pipes.LabeledInstancePipe;
import edu.byu.nlp.data.pipes.Pipes;
import edu.byu.nlp.data.pipes.RegexpTokenizer;
import edu.byu.nlp.data.pipes.SerialLabeledInstancePipeBuilder;
import edu.byu.nlp.data.types.Dataset;
import edu.byu.nlp.data.types.SparseFeatureVector;
import edu.byu.nlp.dataset.Datasets;
import edu.byu.nlp.util.Indexer;
import edu.byu.nlp.util.Nullable;

/**
 * Creates a dataset from a data source of documents. This includes creating
 * count vectors, performing feature selection, and indexing the labels.
 *
 * @author rah67
 * @author plf1
 * 
 */
public class DocPipes {
	private static final Logger logger = LoggerFactory.getLogger(DocPipes.class);

	public static enum Doc2FeaturesMethod {
		WORD_COUNTS, WORD2VEC
	};

	private DocPipes() {
	}

	public static LabeledInstancePipe<String, String, String, String> indexToDocPipe(FileObject baseDir,
			FileObject indexDir) {

		return new SerialLabeledInstancePipeBuilder<String, String, String, String>()
				.add(Pipes.oneToManyLabeledInstancePipe(new IndexFileToLabeledFileList(indexDir)))
				.add(Pipes.labeledInstanceTransformingPipe(new FilenameToContents(baseDir))).build();
	}

	public static LabeledInstancePipe<String, String, String, String> jsonToDocPipe(Reader jsonReader,
			String jsonReferencedDataDir) throws FileNotFoundException {
		return new SerialLabeledInstancePipeBuilder<String, String, String, String>().add(
				Pipes.oneToManyLabeledInstancePipe(new JSONFileToAnnotatedDocumentList(jsonReader,
						jsonReferencedDataDir))).build();
	}

	/**
	 * Creates a data set from the data source. if
	 * {@code featureSelectorFactory} is not null, then performs feature
	 * selection as well.
	 */
	public static Dataset createDataset(DataSource<List<List<String>>, String> src,
			Doc2FeaturesMethod doc2FeatureMethod, @Nullable FeatureSelectorFactory<String> featureSelectorFactory,
			@Nullable Integer featureNormalizationConstant) throws IOException {
		// Index the data (words, labels, instances, annotators)
		IndexerCalculator<String, String> stats = IndexerCalculator.calculate(src.getLabeledInstances());
		Indexer<String> wordIndex = stats.getWordIndexer();
		Indexer<String> labelIndex = stats.getLabelIndexer();
		Indexer<Long> instanceIdIndexer = stats.getInstanceIdIndexer();
		Indexer<Long> annotatorIdIndexer = stats.getAnnotatorIdIndexer();

		// post-processing
		labelIndex = removeNullLabel(labelIndex);
		wordIndex = reduceVocab(src, featureSelectorFactory, wordIndex);

    // prepare feature extractor
    Function<List<List<String>>, SparseFeatureVector> featureExtractor = Functions.compose(new CountNormalizer(
        featureNormalizationConstant), new CountVectorizer<String>(wordIndex));
		
		// convert documents to feature vectors
		LabeledInstancePipe<List<List<String>>, String, SparseFeatureVector, Integer> vectorizer = new SerialLabeledInstancePipeBuilder<List<List<String>>, String, List<List<String>>, String>()
				.addLabelTransform(new FieldIndexer<String>(labelIndex))
				.addAnnotatorIdTransform(FieldIndexer.cast2Long(new FieldIndexer<Long>(annotatorIdIndexer)))
				.addInstanceIdTransform(FieldIndexer.cast2Long(new FieldIndexer<Long>(instanceIdIndexer)))
				.addDataTransform(featureExtractor).build();

		// activate the pipeline transforms in terms of FlatInstance objects
		List<FlatInstance<SparseFeatureVector, Integer>> vectors = DataSources.cache(DataSources.connect(src,
				vectorizer));

		// convert FlatInstances to a Dataset
		return Datasets.convert(src.getSource(), vectors, wordIndex, labelIndex, instanceIdIndexer, annotatorIdIndexer,
				true);
	}

	/**
	 * Do feature selection (on the wordIndex itself)
	 */
	private static Indexer<String> reduceVocab(DataSource<List<List<String>>, String> src,
			FeatureSelectorFactory<String> featureSelectorFactory, Indexer<String> wordIndex) {
		//
		if (featureSelectorFactory != null) {
			// Index before feature selection (we'll need to do it again later
			// after deciding which features to keep)
			// Create count vectors
			Iterable<FlatInstance<SparseFeatureVector, String>> countVectors = Pipes
					.<List<List<String>>, SparseFeatureVector, String> labeledInstanceDataTransformingPipe(
							new CountVectorizer<String>(wordIndex)).apply(src.getLabeledInstances());

			// Feature selection
			int numFeatures = wordIndex.size();
			BitSet features = featureSelectorFactory.newFeatureSelector(numFeatures).processLabeledInstances(
					countVectors);
			logger.info("Number of features before selection = " + numFeatures);
			wordIndex = wordIndex.retain(features);
			logger.info("Number of features after selection = " + wordIndex.size());
		}
		return wordIndex;
	}

	/**
	 * Index labels eliminate the 'null' label, generated by documents with no
	 * label
	 */
	private static Indexer<String> removeNullLabel(Indexer<String> labelIndex) {
		BitSet validLabels = new BitSet();
		for (int l = 0; l < labelIndex.size(); l++) {
			String label = labelIndex.get(l);
			validLabels.set(l, label != null);
		}
		labelIndex = labelIndex.retain(validLabels);
		return labelIndex;
	}

	public static Function<List<String>, List<String>> sentenceTransform(final Function<String, String> sentenceTransform) {
		return new Function<List<String>, List<String>>() {
			@Override
			public List<String> apply(List<String> doc) {
				List<String> xdoc = Lists.newArrayList();
				for (String sent : doc) {
				  String xsent = sentenceTransform.apply(sent);
				  if (xsent!=null){
				    xdoc.add(xsent);
				  }
				}
				return xdoc;
			}
		};
	}
	
	public static Function<List<List<String>>, List<List<String>>> tokenTransform(final Function<String, String> tokenTransform) {
		return new Function<List<List<String>>, List<List<String>>>() {
			@Override
			public List<List<String>> apply(List<List<String>> doc) {
				List<List<String>> xdoc = Lists.newArrayList();
				for (List<String> sent : doc) {
					List<String> xsent = Lists.newArrayList();
					for (String word : sent) {
						String xword = tokenTransform.apply(word);
						if (xword!=null){
						  xsent.add(xword);
						}
					}
					xdoc.add(xsent);
				}
				return xdoc;
			}
		};
	}

	public static Function<List<String>,List<List<String>>> tokenSplitter(final Function<String,List<String>> sentenceSplitter){
		return new Function<List<String>, List<List<String>>>() {
			@Override
			public List<List<String>> apply(List<String> doc) {
				List<List<String>> xdoc = Lists.newArrayList();
				for (String sent: doc){
					xdoc.add(sentenceSplitter.apply(sent));
				}
				return xdoc;
			}
		};
	}
	
	public static Function<String, List<String>> McCallumAndNigamTokenizer() {
		return Functions.compose(new RegexpTokenizer("[a-zA-Z]+"), new Downcase());
	}

	
	public static final String ENGLISH_SENTENCE_DETECTOR = "en-sent.bin";
	public static Function<String, List<String>> opennlpSentenceSplitter() throws IOException {
	  
	  URL modelUrl = Thread.currentThread().getContextClassLoader().getResource(ENGLISH_SENTENCE_DETECTOR);
		File modelFile = new File(modelUrl.getFile());
		final SentenceDetectorME detector = new SentenceDetectorME(new SentenceModel(modelFile));
		
		return new Function<String, List<String>>() {
			@Override
			public List<String> apply(String doc) {
				ArrayList<String> retval = Lists.newArrayList(detector.sentDetect(doc));
				return retval;
			}
		};
		
	}


	
}
