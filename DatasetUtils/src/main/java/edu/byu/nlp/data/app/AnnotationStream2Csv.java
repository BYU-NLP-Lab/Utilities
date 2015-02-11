/**
 * Copyright 2015 Brigham Young University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.byu.nlp.data.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.apache.commons.vfs2.FileSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.io.Files;

import edu.byu.nlp.data.FlatInstance;
import edu.byu.nlp.data.docs.CountCutoffFeatureSelectorFactory;
import edu.byu.nlp.data.docs.FeatureSelectorFactories;
import edu.byu.nlp.data.docs.JSONDocumentDatasetBuilder;
import edu.byu.nlp.data.docs.TokenizerPipes;
import edu.byu.nlp.data.docs.TopNPerDocumentFeatureSelectorFactory;
import edu.byu.nlp.data.types.Dataset;
import edu.byu.nlp.data.types.DatasetInstance;
import edu.byu.nlp.data.types.SparseFeatureVector;
import edu.byu.nlp.dataset.Datasets;
import edu.byu.nlp.io.Paths;
import edu.byu.nlp.util.jargparser.ArgumentParser;
import edu.byu.nlp.util.jargparser.annotations.Option;

/**
 * @author plf1
 *
 * Read in a json annotation stream and output a csv file capturing the 
 * properties of the annotations (for further analysis). 
 * Rows depend on the setting of the --row parameter. 
 * Columns include annotator id, start time, end time, annotation value, 
 * instance source, etc.
 */
public class AnnotationStream2Csv {
  private static Logger logger = LoggerFactory.getLogger(AnnotationStream2Csv.class);

  private static enum ROW {ANNOTATION,INSTANCE};
  @Option(help = "If ")
  private static ROW row = ROW.ANNOTATION;
  
  @Option(help = "A json annotation stream containing annotations to be fitted.")
  private static String jsonStream = "/aml/data/plf1/cfgroups/cfgroups1000.json"; 

  @Option(help = "The file where csv should be written")
  private static String out = null; 
  
  
  public static void main(String[] args) throws IOException{
    // parse CLI arguments
    new ArgumentParser(AnnotationStream2Csv.class).parseArgs(args);
    Preconditions.checkNotNull(jsonStream,"You must provide a valid --json-stream!");
    
    Dataset data = readData(jsonStream);

    // optionally aggregate by instance
    String header = "annotator,start,end,annotation,label,source,num_annotations,num_annotators\n";
    
    // iterate over instances and (optionally) annotations
    final StringBuilder bld = new StringBuilder();
    for (DatasetInstance inst: data){
    	
    	switch(row){
		case ANNOTATION:
			for (FlatInstance<SparseFeatureVector, Integer> ann: inst.getAnnotations().getRawLabelAnnotations()){
				bld.append(ann.getAnnotator()+",");
				bld.append(ann.getStartTimestamp()+",");
				bld.append(ann.getEndTimestamp()+",");
				bld.append(ann.getLabel()+",");
				bld.append(inst.getLabel()+",");
				bld.append(inst.getInfo().getSource()+",");
				bld.append("NA,");
				bld.append("NA");
				bld.append("\n");
			}
			break;
		case INSTANCE:				
			bld.append("NA,");
			bld.append("NA,");
			bld.append("NA,");
			bld.append("NA,");
			bld.append(inst.getLabel()+",");
			bld.append(inst.getInfo().getSource()+",");
			bld.append(inst.getInfo().getNumAnnotations()+",");
			bld.append(inst.getInfo().getNumAnnotators());
			bld.append("\n");
			break;
		default:
			break;
    	}
    }
    
    // output to console
    if (out==null){
    	System.out.println(header);
    	System.out.println(bld.toString());
    }
    else{
        File outfile = new File(out);
        Files.write(header, outfile, Charsets.UTF_8);
        Files.append(bld, outfile, Charsets.UTF_8);
    }
    
  }

  private static Dataset readData(String jsonStream) throws FileSystemException, FileNotFoundException {
    // these parameters are not important since we will ignore the data itself and concentrate only on annotations
    // in this script
    int featureCountCutoff = -1;
    int topNFeaturesPerDocument = -1;
    Integer featureNormalizer = null;
    Function<String, String> docTransform = null;
    Function<List<String>, List<String>> tokenTransform = null;
    
    // data reader pipeline per dataset
    // build a dataset, doing all the tokenizing, stopword removal, and feature normalization
    String folder = Paths.directory(jsonStream);
    String file = Paths.baseName(jsonStream);
    Dataset data = new JSONDocumentDatasetBuilder(folder, file, 
          docTransform, TokenizerPipes.McCallumAndNigam(), tokenTransform, 
          FeatureSelectorFactories.conjoin(
              new CountCutoffFeatureSelectorFactory<String>(featureCountCutoff), 
              (topNFeaturesPerDocument<0)? null: new TopNPerDocumentFeatureSelectorFactory<String>(topNFeaturesPerDocument)),
          featureNormalizer)
          .dataset();
      
    // Postprocessing: remove all documents with duplicate sources or empty features
    data = Datasets.filteredDataset(data, Predicates.and(Datasets.filterDuplicateSources(), Datasets.filterNonEmpty()));
    
    logger.info("Number of labeled instances = " + data.getInfo().getNumDocumentsWithObservedLabels());
    logger.info("Number of unlabeled instances = " + data.getInfo().getNumDocumentsWithoutObservedLabels());
    logger.info("Number of tokens = " + data.getInfo().getNumTokens());
    logger.info("Number of features = " + data.getInfo().getNumFeatures());
    logger.info("Number of classes = " + data.getInfo().getNumClasses());
    logger.info("Average Document Size = " + (data.getInfo().getNumTokens()/data.getInfo().getNumDocuments()));

    return data;
  }
  
}
