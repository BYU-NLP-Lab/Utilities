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

import org.apache.commons.vfs2.FileSystemException;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import edu.byu.nlp.data.streams.DataStream;
import edu.byu.nlp.data.streams.DataStreams;
import edu.byu.nlp.data.streams.FieldIndexer;
import edu.byu.nlp.data.streams.IndexerCalculator;
import edu.byu.nlp.data.streams.JSONFileToAnnotatedDocumentList;
import edu.byu.nlp.data.types.DataStreamInstance;
import edu.byu.nlp.data.types.Dataset;
import edu.byu.nlp.dataset.Datasets;
import edu.byu.nlp.util.Indexers;
import edu.byu.nlp.util.Maps2;
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
  private Function<String, Iterable<String>> sentenceSplitter;
  private Function<String, Iterable<String>> tokenizer;
  private Function<String, String> tokenTransform;
  private final FeatureSelectorFactory featureSelectorFactory;
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
      @Nullable Function<String, Iterable<String>> sentenceSplitter,
      @Nullable Function<String, Iterable<String>> tokenizer,
      @Nullable Function<String, String> tokenTransform,
      FeatureSelectorFactory featureSelectorFactory,
      @Nullable Integer featureNormalizationConstant) {
	  this.jsonAnnotationStream=filename;
	  this.jsonReferencedDataDir=new File(basedir).getAbsolutePath();
    this.docTransform = docTransform;
    this.sentenceSplitter=sentenceSplitter;
    this.tokenizer = tokenizer;
    this.tokenTransform=tokenTransform;
    this.featureSelectorFactory = featureSelectorFactory;
    this.featureNormalizationConstant=featureNormalizationConstant;
  }

  public Dataset dataset() throws IOException {

    // index directory to index filenames
    @SuppressWarnings("unchecked")
    DataStream stream = 
      DataStream.withSource(jsonAnnotationStream.toString(), Lists.newArrayList(Maps2.<String,Object>hashmapOf(DataStreamInstance.DATA, jsonAnnotationStream)))
      // index filenames to data filenames
      .oneToMany(new JSONFileToAnnotatedDocumentList(jsonReferencedDataDir, DataStreamInstance.DATA))
      // transform documents (e.g., remove email headers, transform emoticons)
      .transform(DataStreams.Transforms.transformFieldValue(DataStreamInstance.DATA, docTransform))
      // split sentences (data=List<String>)
      .transform(DataStreams.Transforms.transformFieldValue(DataStreamInstance.DATA, sentenceSplitter))
      // tokenize documents (data=List<List<String>>)
      .transform(DataStreams.Transforms.transformIterableFieldValues(DataStreamInstance.DATA, tokenizer))
      // transform tokens (e.g., remove stopwords, stemmer, remove short words)
      .transform(DataStreams.Transforms.transformIterableIterableFieldValues(DataStreamInstance.DATA, tokenTransform))
    ;

    // feature selection
    IndexerCalculator<String, String> indexers = IndexerCalculator.calculate(stream);
    indexers.setLabelIndexer(Indexers.removeNullLabel(indexers.getLabelIndexer()));
    indexers.setWordIndexer(DocPipes.selectFeatures(stream, featureSelectorFactory, indexers.getWordIndexer()));
      
    // convert data to vectors and labels to numbers
    stream = stream.
      transform(DataStreams.Transforms.transformFieldValue(DataStreamInstance.LABEL, new FieldIndexer<String>(indexers.getLabelIndexer())))
      .transform(DataStreams.Transforms.transformFieldValue(DataStreamInstance.ANNOTATION, new FieldIndexer<String>(indexers.getLabelIndexer())))
      .transform(DataStreams.Transforms.transformFieldValue(DataStreamInstance.SOURCE, new FieldIndexer<String>(indexers.getInstanceIdIndexer())))
      .transform(DataStreams.Transforms.transformFieldValue(DataStreamInstance.ANNOTATOR, new FieldIndexer<String>(indexers.getAnnotatorIdIndexer())))
      .transform(DataStreams.Transforms.transformFieldValue(DataStreamInstance.DATA, new CountVectorizer<String>(indexers.getWordIndexer())))
      .transform(DataStreams.Transforms.transformFieldValue(DataStreamInstance.DATA, new CountNormalizer(featureNormalizationConstant)))
      ;

    // convert FlatInstances to a Dataset
    return Datasets.convert(stream.getName(), stream, indexers, true);
    
  }
  
}
