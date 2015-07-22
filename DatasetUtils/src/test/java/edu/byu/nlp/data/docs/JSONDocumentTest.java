/**
 * Copyright 2013 Brigham Young University
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

import java.io.IOException;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.google.common.collect.Sets;

import edu.byu.nlp.data.types.Dataset;
import edu.byu.nlp.data.types.DatasetInstance;
import edu.byu.nlp.data.util.JsonDatasetMocker;
import edu.byu.nlp.dataset.Datasets;
import edu.byu.nlp.util.Indexer;
import edu.byu.nlp.util.Pair;

/**
 * @author pfelt
 * 
 */
public class JSONDocumentTest {
 

  /**
   * Test method for {@link edu.byu.nlp.data.pipes.StopWordRemover#apply(java.util.List)}.
 * @throws IOException 
   */
  @Test
  public void testBuildDataset() throws IOException {
    Dataset dataset = JsonDatasetMocker.buildTestDatasetFromJson(JsonDatasetMocker.jsonInstances1(System.currentTimeMillis()));
    Assertions.assertThat(dataset.getInfo().getNumDocumentsWithObservedLabels()).isEqualTo(3);
    Assertions.assertThat(dataset.getInfo().getNumDocumentsWithoutObservedLabels()).isEqualTo(5);
    Assertions.assertThat(dataset.getInfo().getNumDocuments()).isEqualTo(8);
    Indexer<String> instanceIndexer = dataset.getInfo().getInstanceIdIndexer();
    
    Pair<? extends Dataset, ? extends Dataset> partitions = Datasets.divideInstancesWithObservedLabels(dataset);
    Dataset labeledData = partitions.getFirst();
    Dataset unlabeledData = partitions.getSecond();
    
    // check labeled data
    Assertions.assertThat(labeledData.getInfo().getNumDocuments()).isEqualTo(3);
    for (DatasetInstance inst: labeledData){
      Assertions.assertThat(Sets.newHashSet("1","2","3").contains(inst.getInfo().getRawSource()));
      Assertions.assertThat(
          (inst.getInfo().getRawSource().equals("1") && inst.getInfo().getNumAnnotations()==2) ||
          (inst.getInfo().getRawSource().equals("2") && inst.getInfo().getNumAnnotations()==0) ||
          (inst.getInfo().getRawSource().equals("3") && inst.getInfo().getNumAnnotations()==0)  
          );
      Assertions.assertThat(inst.getObservedLabel()).isNotEqualTo(dataset.getInfo().getLabelIndexer().indexOf(null));
      Assertions.assertThat(inst.hasObservedLabel()).isTrue();
      Assertions.assertThat(inst.getLabel()).isNotEqualTo(dataset.getInfo().getLabelIndexer().indexOf(null));
      Assertions.assertThat(inst.hasLabel()).isTrue();
    }
    // check unlabeled data
    Assertions.assertThat(unlabeledData.getInfo().getNumDocuments()).isEqualTo(5);
    for (DatasetInstance inst: unlabeledData){
      Assertions.assertThat(Sets.newHashSet("4","five","six","7","8").contains(inst.getInfo().getRawSource()));
      Assertions.assertThat(
          (inst.getInfo().getRawSource().equals("4") && inst.getInfo().getNumAnnotations()==2) ||
          (inst.getInfo().getRawSource().equals("five") && inst.getInfo().getNumAnnotations()==1) ||
          (inst.getInfo().getRawSource().equals("six") && inst.getInfo().getNumAnnotations()==0) ||
          (inst.getInfo().getRawSource().equals("7") && inst.getInfo().getNumAnnotations()==1) ||
          (inst.getInfo().getRawSource().equals("8") && inst.getInfo().getNumAnnotations()==1)   
          );
      
      Assertions.assertThat(inst.asFeatureVector().sum()).isEqualTo(1);
      Assertions.assertThat(!inst.hasObservedLabel());
      
    }
    
  }
  
  
}
