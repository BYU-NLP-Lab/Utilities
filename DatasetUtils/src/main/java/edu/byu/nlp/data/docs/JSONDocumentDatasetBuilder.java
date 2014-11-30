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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.vfs2.FileSystemException;

import com.google.common.base.Function;

import edu.byu.nlp.annotationinterface.java.AnnotationInterfaceJavaUtils;
import edu.byu.nlp.data.FlatInstance;
import edu.byu.nlp.data.FlatLabeledInstance;
import edu.byu.nlp.data.pipes.DataSource;
import edu.byu.nlp.data.pipes.DataSources;
import edu.byu.nlp.data.pipes.LabeledInstancePipe;
import edu.byu.nlp.data.pipes.SerialLabeledInstancePipeBuilder;
import edu.byu.nlp.data.types.Dataset;
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
  private final LabeledInstancePipe<String, String, List<String>, String> tokenizerPipe;
  private Function<List<String>, List<String>> tokenTransform;
  private final FeatureSelectorFactory<String> featureSelectorFactory;
  private Integer featureNormalizationConstant;
  private Reader jsonReader;
  private RandomGenerator rnd;
  private String source;
private String jsonReferencedDataDir;

  public JSONDocumentDatasetBuilder(String basedir, String filename,
      @Nullable Function<String, String> docTransform,
      @Nullable LabeledInstancePipe<String, String, List<String>, String> tokenizerPipe,
      @Nullable Function<List<String>, List<String>> tokenTransform,
      FeatureSelectorFactory<String> featureSelectorFactory, RandomGenerator rnd) 
          throws FileSystemException, FileNotFoundException {
    this(basedir, filename, docTransform, tokenizerPipe, tokenTransform, featureSelectorFactory, null, rnd);
  }

  private static Reader readerOf(String jsonFile) throws FileNotFoundException{
    try {
      return new BufferedReader(new InputStreamReader(new FileInputStream(jsonFile),"utf-8"));
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Invalid json file",e);
    }
  }
  
  public JSONDocumentDatasetBuilder(String basedir, String filename,
      @Nullable Function<String, String> docTransform,
      @Nullable LabeledInstancePipe<String, String, List<String>, String> tokenizerPipe,
      @Nullable Function<List<String>, List<String>> tokenTransform,
      FeatureSelectorFactory<String> featureSelectorFactory,
      @Nullable Integer featureNormalizationConstant, RandomGenerator rnd) throws FileSystemException, FileNotFoundException {
    this(basedir+"/"+filename, new File(basedir).getParent(), readerOf(basedir+"/"+filename), 
        docTransform, tokenizerPipe, tokenTransform, featureSelectorFactory, featureNormalizationConstant, rnd);
  }
  
  /**
   * See class description.
   * 
   * @throws FileSystemException if there is a problem finding the specified directories on the
   *     filesystem.
   */
  public JSONDocumentDatasetBuilder(String source, String jsonReferencedDataDir, Reader jsonReader,
      @Nullable Function<String, String> docTransform,
      @Nullable LabeledInstancePipe<String, String, List<String>, String> tokenizerPipe,
      @Nullable Function<List<String>, List<String>> tokenTransform,
      FeatureSelectorFactory<String> featureSelectorFactory,
      @Nullable Integer featureNormalizationConstant, RandomGenerator rnd) {
		this.source=source;
		this.jsonReferencedDataDir=jsonReferencedDataDir;
	    this.jsonReader=jsonReader;
	    this.docTransform = docTransform;
	    this.tokenizerPipe = tokenizerPipe;
	    this.tokenTransform=tokenTransform;
	    this.featureSelectorFactory = featureSelectorFactory;
	    this.featureNormalizationConstant=featureNormalizationConstant;
	    this.rnd=rnd;
  }

  public Dataset dataset() throws FileNotFoundException {
    // This pipe leaves data in the form it is expected to be in at test time
    LabeledInstancePipe<String, String, String, String> indexToDocPipe = DocPipes.jsonToDocPipe(jsonReader, jsonReferencedDataDir, rnd);

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

    // prime the pump: start the pipe with a dummy instance 
    DataSource<String, String> jsonSource = DataSources.<String,String>from(source, Collections.singletonList(
    		(FlatInstance<String,String>) new FlatLabeledInstance<String,String>(
    				AnnotationInterfaceJavaUtils.newLabeledInstance("", "", "", false))));
    // connect the rest of the pipe
    DataSource<List<String>, String> docSource = DataSources.connect(jsonSource, combinedPipe);
    
    // Cache the data to avoid multiple disk reads
    List<FlatInstance<List<String>, String>> cachedData = DataSources.cache(docSource);

    //
    // Cross-fold validation would create a new pipe factory for each fold.
    // If we have a static test set, we would only do this on the training data
    //
    Dataset dataset = DocPipes.createDataset(
    		DataSources.from(source, cachedData), featureSelectorFactory, featureNormalizationConstant);
    
//    // first null label marks the beginning of "unlabeled" data
//    // this arrangement is enforced by JSONFileToAnnotatedDocumentList
//    int nullLabel = dataset.getInfo().getLabelIndexer().indexOf(null);
//    int firstNullLabel=0;
//    for (DatasetInstance dat: dataset){
//      if (dat.getLabel()==nullLabel){
//        break;
//      }
//      firstNullLabel++;
//    }
//    // FIXME: exclude unlabeled data
//    dataset.hideLabels(dataset.labeledData().size()-firstNullLabel);
    
    return dataset;
  }
}
