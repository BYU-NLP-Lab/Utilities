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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.BitSet;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Functions;

import edu.byu.nlp.data.FlatInstance;
import edu.byu.nlp.data.pipes.DataSource;
import edu.byu.nlp.data.pipes.DataSources;
import edu.byu.nlp.data.pipes.FieldIndexer;
import edu.byu.nlp.data.pipes.FilenameToContents;
import edu.byu.nlp.data.pipes.IndexFileToLabeledFileList;
import edu.byu.nlp.data.pipes.IndexerCalculator;
import edu.byu.nlp.data.pipes.JSONFileToAnnotatedDocumentList;
import edu.byu.nlp.data.pipes.LabeledInstancePipe;
import edu.byu.nlp.data.pipes.Pipes;
import edu.byu.nlp.data.pipes.SerialLabeledInstancePipeBuilder;
import edu.byu.nlp.data.types.Dataset;
import edu.byu.nlp.data.types.SparseFeatureVector;
import edu.byu.nlp.dataset.Datasets;
import edu.byu.nlp.util.Indexer;
import edu.byu.nlp.util.Nullable;
import edu.byu.nlp.util.Pair;

/**
 * Creates a dataset from a data source of documents. This includes creating count vectors,
 * performing feature selection, and indexing the labels.
 *
 * @author rah67
 * @author plf1
 * 
 */
public class DocPipes {
  private static final Logger logger = LoggerFactory.getLogger(DocPipes.class);

  public static enum Doc2FeaturesMethod {WORD_COUNTS, WORD2VEC};
  
  private DocPipes() {}

  public static LabeledInstancePipe<String, String, String, String> indexToDocPipe(FileObject baseDir, FileObject indexDir) {
	   
    return new SerialLabeledInstancePipeBuilder<String, String, String, String>()
            .add(Pipes.oneToManyLabeledInstancePipe(new IndexFileToLabeledFileList(indexDir)))
            .add(Pipes.labeledInstanceTransformingPipe(new FilenameToContents(baseDir)))
            .build();
  }

  public static LabeledInstancePipe<String, String, String, String> jsonToDocPipe(Reader jsonReader, String jsonReferencedDataDir) throws FileNotFoundException {
    return new SerialLabeledInstancePipeBuilder<String, String, String, String>()
            .add(Pipes.oneToManyLabeledInstancePipe(new JSONFileToAnnotatedDocumentList(jsonReader, jsonReferencedDataDir)))
            .build();
  }
  
  public static Dataset createDataset(DataSource<List<String>, String> rawData) throws IOException {
    return createDataset(rawData, Doc2FeaturesMethod.WORD_COUNTS, null);
  }

  public static Dataset createDataset(
      DataSource<List<String>, String> src,
      Doc2FeaturesMethod doc2FeatureMethod,
      @Nullable FeatureSelectorFactory<String> featureSelectorFactory) throws IOException{
    return createDataset(src, doc2FeatureMethod, featureSelectorFactory, null);
  }
  /**
   * Creates a data set from the data source. if {@code featureSelectorFactory} is not null,
   * then performs feature selection as well.
   */
  public static Dataset createDataset(
      DataSource<List<String>, String> src,
      Doc2FeaturesMethod doc2FeatureMethod,
      @Nullable FeatureSelectorFactory<String> featureSelectorFactory,
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
    Pair<Indexer<String>, ? extends Function<List<String>, SparseFeatureVector>> featureInfo = featureExtraction(src, doc2FeatureMethod, featureNormalizationConstant, wordIndex);
    wordIndex = featureInfo.getFirst();
    Function<List<String>, SparseFeatureVector> featureExtractor = featureInfo.getSecond();
    
    // convert documents to feature vectors
    LabeledInstancePipe<List<String>, String, SparseFeatureVector, Integer> vectorizer =
        new SerialLabeledInstancePipeBuilder<List<String>, String, List<String>, String>()
            .addLabelTransform(new FieldIndexer<String>(labelIndex))
            .addAnnotatorIdTransform(FieldIndexer.cast2Long(new FieldIndexer<Long>(annotatorIdIndexer)))
            .addInstanceIdTransform(FieldIndexer.cast2Long(new FieldIndexer<Long>(instanceIdIndexer)))
            .addDataTransform(featureExtractor)
            .build();

    // activate the pipeline transforms in terms of FlatInstance objects
	List<FlatInstance<SparseFeatureVector, Integer>> vectors = DataSources.cache(DataSources.connect(src, vectorizer));

	// convert FlatInstances to a Dataset
	return Datasets.convert(src.getSource(), vectors, wordIndex, labelIndex, instanceIdIndexer, annotatorIdIndexer, true);
  }

	/**
	 * Calculates Document features in several ways
	 */
	private static Pair<Indexer<String>, ? extends Function<List<String>, SparseFeatureVector>> featureExtraction(DataSource<List<String>, String> src, 
			Doc2FeaturesMethod doc2FeatureMethod, 
			Integer featureNormalizationConstant,
			Indexer<String> wordIndex) throws IOException {
		
		switch (doc2FeatureMethod) {
		case WORD_COUNTS:
			// extract features
			Function<List<String>, SparseFeatureVector> featureExtractor =
			Functions.compose(
					new CountNormalizer(featureNormalizationConstant), 
					new CountVectorizer<String>(wordIndex));
			return Pair.of(wordIndex, featureExtractor);
		case WORD2VEC:
			Word2VecCountVectorizer vectorizer = Word2VecCountVectorizer.build(src);
			
			// the string->index mapping assumed by much of the code base doesn't exist here. We just have 
			// arbitrary feature indices. However, we can ensure that models at least have the right number of 
			// features by constructing an identity mapping word indexer that map, e.g., "0" -> 0.
			wordIndex = new Indexer<>();
			for (int i=0; i<vectorizer.getWordVectorSize(); i++){
				wordIndex.add(""+i);
			}
			return Pair.of(wordIndex, vectorizer);
		default:
			throw new IllegalArgumentException("unknown document2feature method: "+doc2FeatureMethod);
		}
		
	}

	/**
	 * Do feature selection (on the wordIndex itself)
	 */
	private static Indexer<String> reduceVocab(DataSource<List<String>, String> src,
			FeatureSelectorFactory<String> featureSelectorFactory, Indexer<String> wordIndex) {
		// 
	    if (featureSelectorFactory != null) {
	    	// Index before feature selection (we'll need to do it again later after deciding which features to keep)
	    	// Create count vectors
	    	Iterable<FlatInstance<SparseFeatureVector, String>> countVectors =
	    			Pipes.<List<String>,SparseFeatureVector,String>labeledInstanceDataTransformingPipe(
	    					new CountVectorizer<String>(wordIndex)).apply(src.getLabeledInstances());
	
	      // Feature selection
	      int numFeatures = wordIndex.size();
	      BitSet features = featureSelectorFactory.newFeatureSelector(numFeatures).processLabeledInstances(countVectors);
	      logger.info("Number of features before selection = " + numFeatures);
	      wordIndex = wordIndex.retain(features);
	      logger.info("Number of features after selection = " + wordIndex.size());
	    }
		return wordIndex;
	}

  /**
   * Index labels eliminate the 'null' label, generated by documents with no label
   */
	private static Indexer<String> removeNullLabel(Indexer<String> labelIndex) {
	    BitSet validLabels = new BitSet();
	    for (int l=0; l<labelIndex.size(); l++){
	    	String label = labelIndex.get(l);
	    	validLabels.set(l,label!=null);
	    }
	    labelIndex = labelIndex.retain(validLabels);
		return labelIndex;
	}
  
  
}
