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
import java.io.Reader;
import java.util.BitSet;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.vfs2.FileObject;

import edu.byu.nlp.data.FlatInstance;
import edu.byu.nlp.data.pipes.DataSource;
import edu.byu.nlp.data.pipes.DataSources;
import edu.byu.nlp.data.pipes.FieldIndexer;
import edu.byu.nlp.data.pipes.FilenameToContents;
import edu.byu.nlp.data.pipes.IndexFileToLabeledFileList;
import edu.byu.nlp.data.pipes.JSONFileToAnnotatedDocumentList;
import edu.byu.nlp.data.pipes.LabeledInstancePipe;
import edu.byu.nlp.data.pipes.Pipes;
import edu.byu.nlp.data.pipes.SerialLabeledInstancePipeBuilder;
import edu.byu.nlp.data.pipes.IndexerCalculator;
import edu.byu.nlp.data.types.Dataset;
import edu.byu.nlp.data.types.SparseFeatureVector;
import edu.byu.nlp.dataset.Datasets;
import edu.byu.nlp.util.Indexer;
import edu.byu.nlp.util.Nullable;

/**
 * Creates a dataset from a data source of documents. This includes creating count vectors,
 * performing feature selection, and indexing the labels.
 *
 * @author rah67
 * @author plf1
 * 
 */
public class DocPipes {
  private static Logger logger = Logger.getLogger(DocPipes.class.getName());
  
  private DocPipes() {}

  public static LabeledInstancePipe<String, String, String, String> indexToDocPipe(FileObject baseDir, FileObject indexDir) {
	   
    return new SerialLabeledInstancePipeBuilder<String, String, String, String>()
            .add(Pipes.oneToManyLabeledInstancePipe(new IndexFileToLabeledFileList(indexDir)))
            .add(Pipes.labeledInstanceTransformingPipe(new FilenameToContents(baseDir)))
            .build();
  }

  public static LabeledInstancePipe<String, String, String, String> jsonToDocPipe(Reader jsonReader, String jsonReferencedDataDir, RandomGenerator rnd) throws FileNotFoundException {
    return new SerialLabeledInstancePipeBuilder<String, String, String, String>()
            .add(Pipes.oneToManyLabeledInstancePipe(new JSONFileToAnnotatedDocumentList(jsonReader, jsonReferencedDataDir, rnd)))
            .build();
  }
  
  public static Dataset createDataset(DataSource<List<String>, String> rawData) {
    return createDataset(rawData, null);
  }

  public static Dataset createDataset(
      DataSource<List<String>, String> src,
      @Nullable FeatureSelectorFactory<String> featureSelectorFactory) {
    return createDataset(src, featureSelectorFactory, null);
  }
  /**
   * Creates a data set from the data source. if {@code featureSelectorFactory} is not null,
   * then performs feature selection as well.
   */
  public static Dataset createDataset(
      DataSource<List<String>, String> src,
      @Nullable FeatureSelectorFactory<String> featureSelectorFactory,
      @Nullable Integer featureNormalizationConstant) {
    // Index the data (words, labels, instances, annotators)
//    Indexer<String> wordIndex = new WordIndexer<String, String>().processLabeledInstances(src.getLabeledInstances());
//    Indexer<String> labelIndex = new LabelIndexer<List<String>, String>().processLabeledInstances(src.getLabeledInstances());
    IndexerCalculator<String, String> stats = IndexerCalculator.calculate(src.getLabeledInstances());
    Indexer<String> wordIndex = stats.getWordIndexer();
    Indexer<String> labelIndex = stats.getLabelIndexer();
    Indexer<Long> instanceIdIndexer = stats.getInstanceIdIndexer();
    Indexer<Long> annotatorIdIndexer = stats.getAnnotatorIdIndexer();
    
    // Index labels
    // eliminate the 'null' label, generated by documents with no label
    BitSet validLabels = new BitSet();
    for (int l=0; l<labelIndex.size(); l++){
    	String label = labelIndex.get(l);
    	validLabels.set(l,label!=null);
    }
    labelIndex = labelIndex.retain(validLabels);
    int numFeatures = wordIndex.size();

    // Create count vectors
    Iterable<FlatInstance<SparseFeatureVector, String>> countVectors =
    		Pipes.<List<String>,SparseFeatureVector,String>labeledInstanceDataTransformingPipe(
    				new CountVectorizer<String>(wordIndex)).apply(src.getLabeledInstances());

    if (featureSelectorFactory != null) {
      // Feature selection
      BitSet features = featureSelectorFactory.newFeatureSelector(numFeatures).processLabeledInstances(countVectors);
      logger.info("Number of features before selection = " + numFeatures);
      wordIndex = wordIndex.retain(features);
      logger.info("Number of features after selection = " + wordIndex.size());
    }

	// Re-index after feature selection (requires re-creating count vectors)
    LabeledInstancePipe<List<String>, String, SparseFeatureVector, Integer> vectorizer =
        new SerialLabeledInstancePipeBuilder<List<String>, String, List<String>, String>()
            .addLabelTransform(new FieldIndexer<String>(labelIndex))
            .addAnnotatorIdTransform(FieldIndexer.cast2Long(new FieldIndexer<Long>(annotatorIdIndexer)))
            .addInstanceIdTransform(FieldIndexer.cast2Long(new FieldIndexer<Long>(instanceIdIndexer)))
            .addDataTransform(new CountVectorizer<String>(wordIndex))
            .addDataTransform(new CountNormalizer(featureNormalizationConstant))
            .build();

    // activate the pipeline transforms in terms of FlatInstance objects
	List<FlatInstance<SparseFeatureVector, Integer>> vectors = DataSources.cache(DataSources.connect(src, vectorizer));

	// convert FlatInstances to a Dataset
	return Datasets.convert(src.getSource(), vectors, wordIndex, labelIndex, instanceIdIndexer, annotatorIdIndexer, true);
  }
}
