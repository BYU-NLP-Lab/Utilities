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
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import edu.byu.nlp.data.FlatInstance;
import edu.byu.nlp.data.docs.CountCutoffFeatureSelectorFactory;
import edu.byu.nlp.data.docs.DocPipes.Doc2FeaturesMethod;
import edu.byu.nlp.data.docs.FeatureSelectorFactories;
import edu.byu.nlp.data.docs.JSONDocumentDatasetBuilder;
import edu.byu.nlp.data.docs.TokenizerPipes;
import edu.byu.nlp.data.docs.TopNPerDocumentFeatureSelectorFactory;
import edu.byu.nlp.data.types.Dataset;
import edu.byu.nlp.data.types.DatasetInstance;
import edu.byu.nlp.data.types.SparseFeatureVector;
import edu.byu.nlp.dataset.Datasets;
import edu.byu.nlp.io.Paths;
import edu.byu.nlp.util.Enumeration;
import edu.byu.nlp.util.Iterables2;
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

  private static enum ROW {ANNOTATION,INSTANCE,ANNOTATOR};
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
    String header = "annotator,start,end,annotation,label,source,instance_id,num_correct_annotations,num_annotations,cum_num_annotations,num_annotators,cum_num_annotators\n";
    
    // iterate over instances and (optionally) annotations
    final StringBuilder bld = new StringBuilder();
    	
    	switch(row){
		case ANNOTATION:

			// sort all annotations by end time
			Map<FlatInstance<SparseFeatureVector, Integer>, DatasetInstance> ann2InstMap = Maps.newIdentityHashMap();
			List<FlatInstance<SparseFeatureVector, Integer>> annotationList = Lists.newArrayList();
		    for (DatasetInstance inst: data){
		    	for (FlatInstance<SparseFeatureVector, Integer> ann: inst.getAnnotations().getRawLabelAnnotations()){
		    		ann2InstMap.put(ann, inst); // record instance of each annotations
		    		annotationList.add(ann);
		    	}
		    }
			Collections.sort(annotationList, new Comparator<FlatInstance<SparseFeatureVector, Integer>>(){
				@Override
				public int compare(FlatInstance<SparseFeatureVector, Integer> o1, FlatInstance<SparseFeatureVector, Integer> o2) {
			        // no null checking since we want to fail if annotation time is not set. 
			        return Long.compare(
			            o1.getEndTimestamp(),
			            o2.getEndTimestamp());
				}
		    });
			
			Set<Long> annotators = Sets.newHashSet();
			for (Enumeration<FlatInstance<SparseFeatureVector, Integer>> item: Iterables2.enumerate(annotationList)){
				FlatInstance<SparseFeatureVector, Integer> ann = item.getElement();
				DatasetInstance inst = ann2InstMap.get(ann);
				annotators.add(ann.getAnnotator());
				
				bld.append(ann.getAnnotator()+",");
				bld.append(ann.getStartTimestamp()+",");
				bld.append(ann.getEndTimestamp()+",");
				bld.append(ann.getLabel()+",");
				bld.append(inst.getLabel()+",");
				bld.append(inst.getInfo().getSource()+",");
				bld.append(inst.getInfo().getInstanceId()+",");
				bld.append((!inst.hasLabel()? "NA": ann.getLabel()==inst.getLabel()? 1: 0)+","); // num correct
				bld.append(1+","); // num annotations
				bld.append((item.getIndex()+1)+","); // cumulative num annotations
				bld.append(1+","); // num annotators
				bld.append(annotators.size()+""); // cumulative num annotators
				bld.append("\n");
			}
			break;
		case INSTANCE:
			int cumNumAnnotations = 0;
		    for (DatasetInstance inst: data){
		    	cumNumAnnotations += inst.getInfo().getNumAnnotations();
		    	
				int numCorrectAnnotations = 0;
				// sum over all the annotators who put the correct answer (if available)
				if (inst.hasLabel()){
					Integer correctLabel = inst.getLabel();
					for (int j=0; j<data.getInfo().getNumAnnotators(); j++){
						numCorrectAnnotations += inst.getAnnotations().getLabelAnnotations().getRow(j)[correctLabel];
					}
				}
				
				bld.append("NA,");
				bld.append("NA,");
				bld.append("NA,");
				bld.append("NA,");
				bld.append(inst.getLabel()+",");
				bld.append(inst.getInfo().getSource()+",");
				bld.append(inst.getInfo().getInstanceId()+",");
				bld.append(numCorrectAnnotations+",");
				bld.append(inst.getInfo().getNumAnnotations()+",");
				bld.append(cumNumAnnotations+",");
				bld.append(inst.getInfo().getNumAnnotators()+",");
				bld.append("NA"); // cumulative num annotators
				bld.append("\n");
		    }
		    break;
		    
		case ANNOTATOR:
			Multiset<Long> perAnnotatorAnnotationCounts = HashMultiset.create();
			Multiset<Long> perAnnotatorCorrectAnnotationCounts = HashMultiset.create();
		    for (DatasetInstance inst: data){
		    	for (FlatInstance<SparseFeatureVector, Integer> ann: inst.getAnnotations().getRawLabelAnnotations()){
		    		long annotatorId = ann.getAnnotator();
		    		
		    		perAnnotatorAnnotationCounts.add(annotatorId);
		    		
		    		if (inst.getLabel()==ann.getLabel()){
		    			perAnnotatorCorrectAnnotationCounts.add(annotatorId);
		    		}

		    	}
		    }
		    
		    
		    for (Long annotatorId: data.getInfo().getAnnotatorIdIndexer()){

				bld.append(annotatorId+",");
				bld.append("NA,");
				bld.append("NA,");
				bld.append("NA,");
				bld.append("NA,");
				bld.append("NA,");
				bld.append("NA,");
				bld.append(perAnnotatorCorrectAnnotationCounts.count(annotatorId) +",");
				bld.append(perAnnotatorAnnotationCounts.count(annotatorId)+",");
				bld.append("NA,");
				bld.append("1,"); // num annotators
				bld.append("NA"); // cumulative num annotators
				bld.append("\n");
		    }
		    
			break;
			
		default:
			Preconditions.checkArgument(false,"unknown row type: "+row);
			break;
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

  private static Dataset readData(String jsonStream) throws IOException {
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
          docTransform, TokenizerPipes.McCallumAndNigam(), tokenTransform, Doc2FeaturesMethod.WORD_COUNTS,
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
