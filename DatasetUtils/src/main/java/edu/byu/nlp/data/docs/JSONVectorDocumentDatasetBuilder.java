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
import java.util.Map;

import org.apache.commons.vfs2.FileSystemException;

import com.google.common.collect.Lists;

import edu.byu.nlp.data.streams.DataStream;
import edu.byu.nlp.data.streams.DataStreams;
import edu.byu.nlp.data.streams.FieldIndexer;
import edu.byu.nlp.data.streams.IndexerCalculator;
import edu.byu.nlp.data.streams.JSONFileToAnnotatedDocumentList;
import edu.byu.nlp.data.types.DataStreamInstance;
import edu.byu.nlp.data.types.Dataset;
import edu.byu.nlp.data.types.SparseFeatureVector;
import edu.byu.nlp.dataset.Datasets;
import edu.byu.nlp.util.Indexers;
import edu.byu.nlp.util.Maps2;

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

    // index directory to index filenames
    @SuppressWarnings("unchecked")
    DataStream stream = 
      DataStream.withSource(jsonAnnotationStream.toString(), Lists.newArrayList(Maps2.<String,Object>hashmapOf(DataStreamInstance.DATA, jsonAnnotationStream)))
      // index filenames to data filenames
      .oneToMany(new JSONFileToAnnotatedDocumentList(jsonReferencedDataDir, DataStreamInstance.DATA))
      // parse data to sparsefeaturevectors in two stages
      .transform(DataStreams.Transforms.transformFieldValue(DataStreamInstance.DATA, DocPipes.documentVectorToArray()))
      .transform(DataStreams.Transforms.transformFieldValue(DataStreamInstance.DATA, DocPipes.arrayToSparseFeatureVector()))
    ;

    // create indexers 
    IndexerCalculator<String, String> indexers = IndexerCalculator.calculateNonFeatureIndexes(stream);
    indexers.setLabelIndexer(Indexers.removeNullLabel(indexers.getLabelIndexer()));
    int numFeatures = getNumFeatures(stream);
    indexers.setWordIndexer(Indexers.indexerOfStrings(numFeatures)); // identity feature-mapping
      
    // convert labels, annotators, and instances to numbers
    stream = stream.
      transform(DataStreams.Transforms.transformFieldValue(DataStreamInstance.LABEL, new FieldIndexer<String>(indexers.getLabelIndexer())))
      .transform(DataStreams.Transforms.transformFieldValue(DataStreamInstance.ANNOTATION, new FieldIndexer<String>(indexers.getLabelIndexer())))
      .transform(DataStreams.Transforms.transformFieldValue(DataStreamInstance.ANNOTATOR, new FieldIndexer<String>(indexers.getAnnotatorIdIndexer())))
      .transform(DataStreams.Transforms.renameField(DataStreamInstance.SOURCE, DataStreamInstance.RAW_SOURCE))
      .transform(DataStreams.Transforms.transformFieldValue(DataStreamInstance.RAW_SOURCE, DataStreamInstance.SOURCE, new FieldIndexer<String>(indexers.getInstanceIdIndexer())))
      ;

    // convert FlatInstances to a Dataset
    return Datasets.convert(stream.getName(), stream, indexers, true);
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
