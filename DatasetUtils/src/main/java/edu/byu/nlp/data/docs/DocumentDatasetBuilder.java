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

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

import edu.byu.nlp.data.FlatInstance;
import edu.byu.nlp.data.pipes.DataSource;
import edu.byu.nlp.data.pipes.DataSources;
import edu.byu.nlp.data.pipes.DirectoryReader;
import edu.byu.nlp.data.pipes.IndexerCalculator;
import edu.byu.nlp.data.pipes.LabeledInstancePipe;
import edu.byu.nlp.data.types.Dataset;
import edu.byu.nlp.data.types.SparseFeatureVector;
import edu.byu.nlp.dataset.Datasets;
import edu.byu.nlp.util.Indexers;
import edu.byu.nlp.util.Nullable;

/**
 * Builds a dataset of documents. A collection of documents is organized into datasets and each
 * dataset has predefined splits. For instance, the 20_newsgroups collection can have a 4-class
 * dataset that is statically split into a training and a test set. These splits are determined
 * by index files, one per class. Each line of the index file is a path relative to a base
 * directory pointing to a document pertaining to that class. The directory structure for the
 * indices must be as follows:
 * 
 *   {basedir}/indices/{dataset}/{split}/{class}.txt
 * 
 * The documents themselves may reside anywhere below {basedir}.
 * 
 * This class requires a tokenizer pipeline to be specified (see {@link TokenizerPipes} for some
 * common pipes). It also optionally takes a {@code Function} for transforming the document, e.g.,
 * stripping headers. An optional {@code FeatureSelectorFactory} can be specified to perform
 * feature selection. 
 * 
 * @author rah67
 * @author plf1
 * 
 */
public class DocumentDatasetBuilder {
	private static Logger logger = LoggerFactory.getLogger(DocumentDatasetBuilder.class);


  private final FileObject basedir;
  private final FileObject indexDirectory;

  private final Function<String, String> docTransform;
  private Function<String, List<String>> sentenceSplitter;
  private Function<String, List<String>> tokenizer;
  private final FeatureSelectorFactory<String> featureSelectorFactory;
  private Integer featureNormalizationConstant;
  private Function<String, String> tokenTransform;



  /**
   * See class description.
   * 
   * @throws FileSystemException if there is a problem finding the specified directories on the
   *     filesystem.
   */
  public DocumentDatasetBuilder(String basedir, String dataset, String split,
      @Nullable Function<String, String> docTransform,
      @Nullable Function<String, List<String>> sentenceSplitter,
      @Nullable Function<String, List<String>> tokenizer,
      @Nullable Function<String, String> tokenTransform,
      FeatureSelectorFactory<String> featureSelectorFactory,
      @Nullable Integer featureNormalizationConstant) throws FileSystemException {
    // TODO: consider taking the FileObject as a parameter
    FileSystemManager fsManager = VFS.getManager();
    if (fsManager instanceof DefaultFileSystemManager) {
      ((DefaultFileSystemManager) fsManager).setBaseFile(new File("."));
    }
    this.basedir = fsManager.resolveFile(basedir);
    Preconditions.checkNotNull(this.basedir, "%s cannot be resolved", basedir);
    Preconditions.checkArgument(this.basedir.getType() == FileType.FOLDER);
    FileObject indices = this.basedir.getChild("indices");
    Preconditions.checkNotNull(indices, "cannot find indices directory in %s", basedir);
    FileObject datasetDir = indices.getChild(dataset);
    Preconditions.checkNotNull(datasetDir, "cannot find index for dataset %s", dataset);
    this.indexDirectory = datasetDir.getChild(split);
    Preconditions.checkNotNull(indexDirectory, "cannot find split %s", split);
    Preconditions.checkArgument(indexDirectory.getType() == FileType.FOLDER);
    this.docTransform = docTransform;
    this.sentenceSplitter=sentenceSplitter;
    this.tokenizer = tokenizer;
    this.tokenTransform = tokenTransform;
    this.featureSelectorFactory = featureSelectorFactory;
    this.featureNormalizationConstant=featureNormalizationConstant;
  }

  
  public Dataset dataset() throws IOException {
    
    // first pipe - to import input files into strings and do greedy feature transformation/selection (e.g., filter short words)
    LabeledInstancePipe<String, String, List<List<String>>, String> inputPipe = DocPipes.index2SentencePipe(
        DocPipes.indexToDocPipe(basedir), docTransform, sentenceSplitter, tokenizer, tokenTransform);
    
    // apply first pipeline (input)
    List<FlatInstance<List<List<String>>, String>> sentenceData = DataSources.cache(
        DataSources.connect(new DirectoryReader(indexDirectory), inputPipe)); // Cache the data to avoid multiple disk reads
    
    // feature selection
    IndexerCalculator<String, String> indexers = IndexerCalculator.calculate(sentenceData);
    indexers.setLabelIndexer(Indexers.removeNullLabel(indexers.getLabelIndexer()));
    indexers.setWordIndexer(DocPipes.selectFeatures(sentenceData, featureSelectorFactory, indexers.getWordIndexer()));

    // second pipe to convert data to vectors and labels to numbers 
    LabeledInstancePipe<List<List<String>>, String, SparseFeatureVector, Integer> vectorizingPipe = 
        DocPipes.sentence2FeatureVectorPipe(sentenceData, indexers, featureNormalizationConstant);
    
    // apply second pipe (vectorization)
    DataSource<List<List<String>>, String> vectorDatasetSource = DataSources.from(indexDirectory.toString(), sentenceData);
    List<FlatInstance<SparseFeatureVector, Integer>> vectorData = DataSources.cache(DataSources.connect(vectorDatasetSource, vectorizingPipe));

    // convert FlatInstances to a Dataset
    return Datasets.convert(vectorDatasetSource.getSource(), vectorData, indexers, true);
    
  }

}