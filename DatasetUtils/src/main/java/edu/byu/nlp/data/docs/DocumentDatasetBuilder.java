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

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

import edu.byu.nlp.data.FlatInstance;
import edu.byu.nlp.data.docs.DocPipes.Doc2FeaturesMethod;
import edu.byu.nlp.data.pipes.DataSource;
import edu.byu.nlp.data.pipes.DataSources;
import edu.byu.nlp.data.pipes.DirectoryReader;
import edu.byu.nlp.data.pipes.LabeledInstancePipe;
import edu.byu.nlp.data.pipes.SerialLabeledInstancePipeBuilder;
import edu.byu.nlp.data.types.Dataset;
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
 * 
 */
public class DocumentDatasetBuilder {

  private final FileObject basedir;
  private final FileObject indexDirectory;

  private final Function<String, String> docTransform;
  private final LabeledInstancePipe<String, String, List<String>, String> tokenizerPipe;
  private final FeatureSelectorFactory<String> featureSelectorFactory;
  private Integer featureNormalizationConstant;
  private Function<List<String>, List<String>> tokenTransform;
private Doc2FeaturesMethod doc2FeatureMethod;
private String uniqueName;

  public DocumentDatasetBuilder(String basedir, String dataset, String split,
      @Nullable Function<String, String> docTransform, 
      @Nullable LabeledInstancePipe<String, String, List<String>, String> tokenizerPipe,
      @Nullable Function<List<String>, List<String>> tokenTransform,
      Doc2FeaturesMethod doc2FeatureMethod,
      FeatureSelectorFactory<String> featureSelectorFactory) throws FileSystemException {
    this(basedir, dataset, split, docTransform, tokenizerPipe, tokenTransform, doc2FeatureMethod, featureSelectorFactory, null);
  }
  
  /**
   * See class description.
   * 
   * @throws FileSystemException if there is a problem finding the specified directories on the
   *     filesystem.
   */
  public DocumentDatasetBuilder(String basedir, String dataset, String split,
      @Nullable Function<String, String> docTransform,
      @Nullable LabeledInstancePipe<String, String, List<String>, String> tokenizerPipe,
      @Nullable Function<List<String>, List<String>> tokenTransform,
      Doc2FeaturesMethod doc2FeatureMethod,
      FeatureSelectorFactory<String> featureSelectorFactory,
      @Nullable Integer featureNormalizationConstant) throws FileSystemException {
    // TODO: consider taking the FileObject as a parameter
    FileSystemManager fsManager = VFS.getManager();
    if (fsManager instanceof DefaultFileSystemManager) {
      ((DefaultFileSystemManager) fsManager).setBaseFile(new File("."));
    }
    this.uniqueName = Joiner.on('-').join(basedir,dataset,split);
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
    this.tokenizerPipe = tokenizerPipe;
    this.tokenTransform = tokenTransform;
    this.doc2FeatureMethod=doc2FeatureMethod;
    this.featureSelectorFactory = featureSelectorFactory;
    this.featureNormalizationConstant=featureNormalizationConstant;
  }

  
  public Dataset dataset() throws IOException {
    // This pipe leaves data in the form it is expected to be in at test time
    LabeledInstancePipe<String, String, String, String> indexToDocPipe =
        DocPipes.indexToDocPipe(basedir, indexDirectory);

    // Combine the indexToDocPipe, transform (if applicable), and tokenizer.
    SerialLabeledInstancePipeBuilder<String, String, String, String> builder =
        new SerialLabeledInstancePipeBuilder<String, String, String, String>().add(indexToDocPipe);
    if (docTransform != null) {
      builder = builder.addDataTransform(docTransform);
    }
    SerialLabeledInstancePipeBuilder<String, String, List<String>, String> tokenbuilder = builder.add(tokenizerPipe);
    if (tokenTransform!=null){
      tokenbuilder.addDataTransform(tokenTransform);
    }
    LabeledInstancePipe<String, String, List<String>, String> combinedPipe = tokenbuilder.build();

    String source = indexDirectory.toString();
    DataSource<List<String>, String> docSource =
        DataSources.connect(new DirectoryReader(source, indexDirectory), combinedPipe);

    // Cache the data to avoid multiple disk reads
    List<FlatInstance<List<String>, String>> cachedData = DataSources.cache(docSource);
    
    // Cross-fold validation would create a new pipe factory for each fold.
    // If we have a static test set, we would only do this on the training data
    return DocPipes.createDataset(DataSources.from(source, cachedData), doc2FeatureMethod, featureSelectorFactory, featureNormalizationConstant);
  }
  
}

