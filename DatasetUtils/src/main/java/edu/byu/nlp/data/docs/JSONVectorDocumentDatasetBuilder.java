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

import edu.byu.nlp.data.streams.DataStreamSource;
import edu.byu.nlp.data.streams.DataStreamSources;
import edu.byu.nlp.data.streams.FieldIndexer;
import edu.byu.nlp.data.streams.IndexerCalculator;
import edu.byu.nlp.data.types.Dataset;
import edu.byu.nlp.data.types.SparseFeatureVector;
import edu.byu.nlp.dataset.Datasets;
import edu.byu.nlp.util.Indexers;

/**
 * The same as JSONDocumentDatasetBuilder except it expects data 
 * to be in the form of precomputed, serialized feature vectors.
 * 
 * @author pfelt
 * 
 */
public class JSONVectorDocumentDatasetBuilder {

  private String jsonAnnotationStream;
  private String jsonReferencedDataDir;

  
  /**
   * See class description.
   * 
   * @throws FileSystemException if there is a problem finding the specified directories on the
   *     filesystem.
   */
  public JSONVectorDocumentDatasetBuilder(String basedir, String filename) {
	  this.jsonAnnotationStream=filename;
	  this.jsonReferencedDataDir=new File(basedir).getAbsolutePath();
  }

  public Dataset dataset() throws IOException {

    // input pipe parses feature vectors out of files 
    LabeledInstancePipe<String, String, SparseFeatureVector, String> inputPipe = new SerialLabeledInstancePipeBuilder<String, String, String, String>()
    // convert a file system dataset (string) to document contents (String) and labels (String)
    .add(DocPipes.jsonToDocPipe(jsonReferencedDataDir))
    .addDataTransform(DocPipes.documentVectorToArray())
    .addDataTransform(DocPipes.arrayToSparseFeatureVector())
    .build();
    
    // apply first pipeline (input)
    List<FlatInstance<SparseFeatureVector, String>> sentenceData = DataStreamSources.cache(
        DataStreamSources.connect(DataStreamSources.fromPath(jsonAnnotationStream), inputPipe)); 
    
    // indexing pipe converts labels to numbers 
    IndexerCalculator<String, String> indexers = IndexerCalculator.calculateNonFeatureIndexes(sentenceData);
    indexers.setLabelIndexer(Indexers.removeNullLabel(indexers.getLabelIndexer()));
    int numFeatures = getNumFeatures(sentenceData);
    indexers.setWordIndexer(Indexers.indexerOfStrings(numFeatures)); // identity feature-mapping
    
    // index columns
    LabeledInstancePipe<SparseFeatureVector, String, SparseFeatureVector, Integer> indexerPipe = 
        new SerialLabeledInstancePipeBuilder<SparseFeatureVector, String, SparseFeatureVector, String>()
        .addLabelTransform(new FieldIndexer<String>(indexers.getLabelIndexer()))
        .addAnnotatorIdTransform(FieldIndexer.cast2Long(new FieldIndexer<Long>(indexers.getAnnotatorIdIndexer())))
        .addInstanceIdTransform(FieldIndexer.cast2Long(new FieldIndexer<Long>(indexers.getInstanceIdIndexer())))
        .build();
    
    // apply second pipeline (vectorization)
    DataStreamSource<SparseFeatureVector, String> vectorDatasetSource = DataStreamSources.from(jsonAnnotationStream, sentenceData);
    List<FlatInstance<SparseFeatureVector, Integer>> vectorData = DataStreamSources.cache(DataStreamSources.connect(vectorDatasetSource, indexerPipe));

    // convert FlatInstances to a Dataset
    return Datasets.convert(vectorDatasetSource.getSource(), vectorData, indexers, true);
  }

  private int getNumFeatures(List<FlatInstance<SparseFeatureVector, String>> sentenceData) {
    for (FlatInstance<SparseFeatureVector, String> sent: sentenceData){
      if (sent.getData()!=null){
        return sent.getData().length();
      }
    }
    throw new IllegalStateException("None of the reference json items have any data vectors. This is illegal.");
  }
  
}
