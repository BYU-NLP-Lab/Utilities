/**
 * Copyright 2012 Brigham Young University
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
import java.io.IOException;
import java.util.List;

import org.apache.commons.vfs2.FileSystemException;

import com.google.common.base.Function;

import edu.byu.nlp.data.FlatInstance;
import edu.byu.nlp.data.pipes.DataSource;
import edu.byu.nlp.data.pipes.DataSources;
import edu.byu.nlp.data.pipes.IndexerCalculator;
import edu.byu.nlp.data.pipes.LabeledInstancePipe;
import edu.byu.nlp.data.types.Dataset;
import edu.byu.nlp.data.types.SparseFeatureVector;
import edu.byu.nlp.dataset.Datasets;
import edu.byu.nlp.util.Indexers;
import edu.byu.nlp.util.Nullable;

/**
 * Builds a dataset of documents from a JSON file with the following structure: 
 * 
 * [
 *   # An annotated instance
 *   {   batch: 123
 *       source: "http://document/id",
 *       data: "The text of the first document",
 *       label: "TrueLabel",
 *       annotator: "george",
 *       annotation: "SomeLabel"
 *       annotationTime: {   "startTimeSecs":1319123,
 *                           "endTimeSecs":1319198}
 *   },
 *   etc...
 * ]
 *
 * If 'batch' is set, this annotation was received as part
 * of a batch of annotations sharing this number.
 * Annotations in the same batch are reported consecutively.

 * startTimeSecs and endTimeSecs are utc timestamps (number
 * of secs since 1 Jan 1970))
 * 
 * This class requires a tokenizer pipeline to be specified (see {@link TokenizerPipes} for some
 * common pipes). It also optionally takes a {@code Function} for transforming the document, e.g.,
 * stripping headers. An optional {@code FeatureSelectorFactory} can be specified to perform
 * feature selection. 
 * 
 * @author pfelt
 * 
 */
public class JSONDocumentDatasetBuilder {

  private final Function<String, String> docTransform;
  private Function<String, List<String>> sentenceSplitter;
  private Function<String, List<String>> tokenizer;
  private Function<String, String> tokenTransform;
  private final FeatureSelectorFactory<String> featureSelectorFactory;
  private Integer featureNormalizationConstant;
  private String jsonAnnotationStream;
  private String jsonReferencedDataDir;

  
  /**
   * See class description.
   * 
   * @throws FileSystemException if there is a problem finding the specified directories on the
   *     filesystem.
   */
  public JSONDocumentDatasetBuilder(String basedir, String filename,
      @Nullable Function<String, String> docTransform,
      @Nullable Function<String, List<String>> sentenceSplitter,
      @Nullable Function<String, List<String>> tokenizer,
      @Nullable Function<String, String> tokenTransform,
      FeatureSelectorFactory<String> featureSelectorFactory,
      @Nullable Integer featureNormalizationConstant) {
	  this.jsonAnnotationStream=basedir+"/"+filename;
	  this.jsonReferencedDataDir=new File(basedir).getAbsolutePath();
    this.docTransform = docTransform;
    this.sentenceSplitter=sentenceSplitter;
    this.tokenizer = tokenizer;
    this.tokenTransform=tokenTransform;
    this.featureSelectorFactory = featureSelectorFactory;
    this.featureNormalizationConstant=featureNormalizationConstant;
  }

  public Dataset dataset() throws IOException {

    // pipe to import input files into strings and do greedy feature transformation/selection (e.g., filter short words)
    LabeledInstancePipe<String, String, List<List<String>>, String> inputPipe = DocPipes.index2SentencePipe(
        DocPipes.jsonToDocPipe(jsonReferencedDataDir), docTransform, sentenceSplitter, tokenizer, tokenTransform);

    // apply first pipeline (input)
    List<FlatInstance<List<List<String>>, String>> sentenceData = DataSources.cache(
        DataSources.connect(DataSources.fromPath(jsonAnnotationStream), inputPipe)); 

    // feature selection
    IndexerCalculator<String, String> indexers = IndexerCalculator.calculate(sentenceData);
    indexers.setLabelIndexer(Indexers.removeNullLabel(indexers.getLabelIndexer()));
    indexers.setWordIndexer(DocPipes.selectFeatures(sentenceData, featureSelectorFactory, indexers.getWordIndexer()));

    // second pipe to convert data to vectors and labels to numbers 
    LabeledInstancePipe<List<List<String>>, String, SparseFeatureVector, Integer> vectorizingPipe = 
        DocPipes.sentence2FeatureVectorPipe(sentenceData, indexers, featureNormalizationConstant);
    
    // apply second pipe (vectorization)
    DataSource<List<List<String>>, String> vectorDatasetSource = DataSources.from(jsonAnnotationStream, sentenceData);
    List<FlatInstance<SparseFeatureVector, Integer>> vectorData = DataSources.cache(DataSources.connect(vectorDatasetSource, vectorizingPipe));

    // convert FlatInstances to a Dataset
    return Datasets.convert(vectorDatasetSource.getSource(), vectorData, indexers, true);
    
  }
  
}
