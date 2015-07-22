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
import edu.byu.nlp.data.types.SparseFeatureVector;
import edu.byu.nlp.dataset.Datasets;
import edu.byu.nlp.util.Indexers;

/**
 * Does precisely the same thing as DocumentDatasetBuilder, expect it expects a 
 * dataset whose documents contains a vector or real numbers, one number per line.
 * All document vectors must be the same length.
 * These document vectors may have been generated by something like 
 * DatasetUtils/python_datautil/doc2vec.py. 
 * 
 * @author plf1
 * 
 */
public class VectorDocumentDatasetBuilder {
	private static Logger logger = LoggerFactory.getLogger(VectorDocumentDatasetBuilder.class);


  private final FileObject basedir;
  private final FileObject indexDirectory;



  /**
   * See class description.
   * 
   * @throws FileSystemException if there is a problem finding the specified directories on the
   *     filesystem.
   */
  public VectorDocumentDatasetBuilder(String basedir, String dataset, String split) throws FileSystemException {
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
  }

  
  public Dataset dataset() throws IOException {
//    
//    // input pipe parses feature vectors out of files 
//    LabeledInstancePipe<String, String, SparseFeatureVector, String> inputPipe = new SerialLabeledInstancePipeBuilder<String, String, String, String>()
//    // convert a file system dataset (string) to document contents (String) and labels (String)
//    .add(DocPipes.indexToDocPipe(basedir))
//    .addDataTransform(DocPipes.documentVectorToArray())
//    .addDataTransform(DocPipes.arrayToSparseFeatureVector())
//    .build();
//        
//    // apply first pipeline (input)
//    List<FlatInstance<SparseFeatureVector, String>> sentenceData = DataStreamSources.cache(
//        DataStreamSources.connect(new DirectoryReader(indexDirectory), inputPipe)); // Cache the data to avoid multiple disk reads
//    
//    // indexing pipe converts labels to numbers 
//    IndexerCalculator<String, String> indexers = IndexerCalculator.calculateNonFeatureIndexes(sentenceData);
//    indexers.setLabelIndexer(Indexers.removeNullLabel(indexers.getLabelIndexer()));
//    indexers.setWordIndexer(Indexers.indexerOfStrings(sentenceData.get(0).getData().length())); // identity feature-mapping
//    
//    // index columns
//    LabeledInstancePipe<SparseFeatureVector, String, SparseFeatureVector, Integer> indexerPipe = 
//        new SerialLabeledInstancePipeBuilder<SparseFeatureVector, String, SparseFeatureVector, String>()
//        .addLabelTransform(new FieldIndexer<String>(indexers.getLabelIndexer()))
//        .addAnnotatorIdTransform(FieldIndexer.cast2Long(new FieldIndexer<Long>(indexers.getAnnotatorIdIndexer())))
//        .addInstanceIdTransform(FieldIndexer.cast2Long(new FieldIndexer<Long>(indexers.getInstanceIdIndexer())))
//        .build();
//    
//    // apply second pipeline (vectorization)
//    DataStreamSource<SparseFeatureVector, String> vectorDatasetSource = DataStreamSources.from(indexDirectory.toString(), sentenceData);
//    List<FlatInstance<SparseFeatureVector, Integer>> vectorData = DataStreamSources.cache(DataStreamSources.connect(vectorDatasetSource, indexerPipe));
//
//    // convert FlatInstances to a Dataset
//    return Datasets.convert(vectorDatasetSource.getSource(), vectorData, indexers, true);
//    
    

    // index directory to index filenames
    DataStream stream = 
      DataStream.withSource(indexDirectory.toString(), new DirectoryReader(indexDirectory, DataStreamInstance.LABEL).getStream())
      // index filenames to data filenames
      .oneToMany(DataStreams.OneToManys.oneToManyByFieldValue(DataStreamInstance.LABEL, DataStreamInstance.SOURCE, new IndexFileToFileList(indexDirectory)))
      // data filenames to data
      .transform(DataStreams.Transforms.transformFieldValue(DataStreamInstance.SOURCE, DataStreamInstance.DATA, new FilenameToContents(basedir)))
      // transform documents (e.g., remove email headers, transform emoticons)
      .transform(DataStreams.Transforms.transformFieldValue(DataStreamInstance.DATA, DocPipes.documentVectorToArray()))
      .transform(DataStreams.Transforms.transformFieldValue(DataStreamInstance.DATA, DocPipes.arrayToSparseFeatureVector()))
    ;
    ArrayList<Map<String, Object>> instances = Lists.newArrayList(stream); // cache results

    // feature selection
    IndexerCalculator<String, String> indexers = IndexerCalculator.calculate(instances);
    indexers.setLabelIndexer(Indexers.removeNullLabel(indexers.getLabelIndexer()));
    indexers.setWordIndexer(Indexers.indexerOfStrings(getNumFeatures(instances))); // identity feature-mapping
      
    // convert data to vectors and labels to numbers
    stream = DataStream.withSource(indexDirectory.toString(), instances)
      .transform(DataStreams.Transforms.transformFieldValue(DataStreamInstance.LABEL, new FieldIndexer<String>(indexers.getLabelIndexer())))
      .transform(DataStreams.Transforms.transformFieldValue(DataStreamInstance.ANNOTATION, new FieldIndexer<String>(indexers.getLabelIndexer())))
      .transform(DataStreams.Transforms.renameField(DataStreamInstance.SOURCE, DataStreamInstance.RAW_SOURCE))
      .transform(DataStreams.Transforms.transformFieldValue(DataStreamInstance.RAW_SOURCE, DataStreamInstance.SOURCE, new FieldIndexer<String>(indexers.getInstanceIdIndexer())))
      .transform(DataStreams.Transforms.transformFieldValue(DataStreamInstance.ANNOTATOR, new FieldIndexer<String>(indexers.getAnnotatorIdIndexer())))
      ;
    instances = Lists.newArrayList(stream); // cache results

    // convert FlatInstances to a Dataset
    return Datasets.convert(stream.getName(), instances, indexers, true);
  }

  private int getNumFeatures(Iterable<Map<String,Object>> sentenceData) {
    for (Map<String,Object> sent: sentenceData){
      SparseFeatureVector dat = (SparseFeatureVector) DataStreamInstance.getData(sent); 
      if (dat!=null){
        return dat.length();
      }
    }
    throw new IllegalStateException("None of the reference json items have any data vectors. This is illegal.");
  }
  

}

