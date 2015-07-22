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
import java.util.ArrayList;
import java.util.Map;

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
import com.google.common.collect.Lists;

import edu.byu.nlp.data.streams.DataStream;
import edu.byu.nlp.data.streams.DataStreams;
import edu.byu.nlp.data.streams.DirectoryReader;
import edu.byu.nlp.data.streams.FieldIndexer;
import edu.byu.nlp.data.streams.FilenameToContents;
import edu.byu.nlp.data.streams.IndexFileToFileList;
import edu.byu.nlp.data.streams.IndexerCalculator;
import edu.byu.nlp.data.types.DataStreamInstance;
import edu.byu.nlp.data.types.Dataset;
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
  private Function<String, Iterable<String>> sentenceSplitter;
  private Function<String, Iterable<String>> tokenizer;
  private final FeatureSelectorFactory featureSelectorFactory;
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
      @Nullable Function<String, Iterable<String>> sentenceSplitter,
      @Nullable Function<String, Iterable<String>> tokenizer,
      @Nullable Function<String, String> tokenTransform,
      FeatureSelectorFactory featureSelectorFactory,
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

    // index directory to index filenames
    DataStream stream = 
      DataStream.withSource(indexDirectory.toString(), new DirectoryReader(indexDirectory, DataStreamInstance.LABEL).getStream())
      // index filenames to data filenames
      .oneToMany(DataStreams.OneToManys.oneToManyByFieldValue(DataStreamInstance.LABEL, DataStreamInstance.INSTANCE_ID, new IndexFileToFileList(indexDirectory)))
      // data filenames to data
      .transform(DataStreams.Transforms.transformFieldValue(DataStreamInstance.INSTANCE_ID, DataStreamInstance.DATA, new FilenameToContents(basedir)))
      // transform documents (e.g., remove email headers, transform emoticons)
      .transform(DataStreams.Transforms.transformFieldValue(DataStreamInstance.DATA, docTransform))
      // split sentences
      .transform(DataStreams.Transforms.transformFieldValue(DataStreamInstance.DATA, sentenceSplitter))
      // tokenize documents (data=List<List<String>>)
      .transform(DataStreams.Transforms.transformIterableFieldValues(DataStreamInstance.DATA, tokenizer))
      // transform tokens (e.g., remove stopwords, stemmer, remove short words)
      .transform(DataStreams.Transforms.transformIterableIterableFieldValues(DataStreamInstance.DATA, tokenTransform))
    ;
    ArrayList<Map<String, Object>> instances = Lists.newArrayList(stream); // cache results

    // feature selection
    IndexerCalculator<String, String> indexers = IndexerCalculator.calculate(instances);
    indexers.setLabelIndexer(Indexers.removeNullLabel(indexers.getLabelIndexer()));
    indexers.setWordIndexer(DocPipes.selectFeatures(instances, featureSelectorFactory, indexers.getWordIndexer()));
      
    // convert data to vectors and labels to numbers
    stream = DataStream.withSource(indexDirectory.toString(), instances)
      .transform(DataStreams.Transforms.transformFieldValue(DataStreamInstance.LABEL, new FieldIndexer<String>(indexers.getLabelIndexer())))
      .transform(DataStreams.Transforms.transformFieldValue(DataStreamInstance.ANNOTATION, new FieldIndexer<String>(indexers.getLabelIndexer())))
      .transform(DataStreams.Transforms.renameField(DataStreamInstance.INSTANCE_ID, DataStreamInstance.SOURCE))
      .transform(DataStreams.Transforms.transformFieldValue(DataStreamInstance.SOURCE, DataStreamInstance.INSTANCE_ID, new FieldIndexer<String>(indexers.getInstanceIdIndexer())))
      .transform(DataStreams.Transforms.transformFieldValue(DataStreamInstance.ANNOTATOR, new FieldIndexer<String>(indexers.getAnnotatorIdIndexer())))
      .transform(DataStreams.Transforms.transformFieldValue(DataStreamInstance.DATA, new CountVectorizer<String>(indexers.getWordIndexer())))
      .transform(DataStreams.Transforms.transformFieldValue(DataStreamInstance.DATA, new CountNormalizer(featureNormalizationConstant)))
      ;
    instances = Lists.newArrayList(stream); // cache results
    
    // convert FlatInstances to a Dataset
    return Datasets.convert(stream.getName(), instances, indexers, true);
    
  }

}