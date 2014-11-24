/**
 * Copyright 2014 Brigham Young University
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
package edu.byu.nlp.pipes.doc;

import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.stat.inference.TestUtils;
import org.fest.assertions.Assertions;
import org.junit.Test;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.byu.nlp.data.types.Dataset;
import edu.byu.nlp.data.types.DatasetInstance;
import edu.byu.nlp.dataset.Datasets;
import edu.byu.nlp.dataset.DatasetsTestUtil;
import edu.byu.nlp.util.DoubleArrays;
import edu.byu.nlp.util.Pair;

/**
 * @author pfelt
 *
 */
public class DatasetsTest {

  public static String jsonInstances(long seed){ 
    List<String> jsonInstances = Lists.newArrayList(
        // "1" has 2 annotations + label
      "{\"batch\": 0, \"data\":\"ABC\", \"endTime\":0, \"label\":\"0\",                           \"source\":1,     \"startTime\":0 }", // labeled 
      "{\"batch\": 1, \"data\":\"ABC\", \"endTime\":1, \"annotation\":\"0\", \"annotator\":\"A\", \"source\":1,     \"startTime\":0 }", // annotation to the same doc
      "{\"batch\": 2, \"data\":\"ABC\", \"endTime\":2, \"annotation\":\"1\", \"annotator\":\"B\", \"source\":1,     \"startTime\":0 }", // annotation to the same doc
      "{\"batch\": 3, \"data\":\"ABC\", \"endTime\":3,                                            \"source\":1,     \"startTime\":0 }", // bare ref to the same doc
      
      // "2" has 2 annotations + label
      "{\"batch\": 0, \"data\":\"DEF\", \"endTime\":4, \"label\":\"1\",                           \"source\":\"2\", \"startTime\":0 }", // labeled
      "{\"batch\": 0, \"data\":\"DEF\", \"endTime\":5, \"annotation\":\"1\", \"annotator\":\"C\", \"source\":\"2\", \"startTime\":0 }", // annotation
      "{\"batch\": 0, \"data\":\"DEF\", \"endTime\":6, \"annotation\":\"0\", \"annotator\":\"C\", \"source\":\"2\", \"startTime\":0 }", // annotation

      // "3" has label
      "{\"batch\": 0, \"data\":\"DEF\", \"endTime\":7, \"label\":\"0\",                           \"source\":3,     \"startTime\":0 }", // labeled

      // "4" has 2 annotations + label
      "{\"batch\": 0, \"data\":\"DEF\", \"endTime\":8, \"label\":\"1\",                           \"source\":4,     \"startTime\":0 }", // labeled
      "{\"batch\": 0, \"data\":\"DEF\", \"endTime\":9, \"annotation\":\"0\", \"annotator\":\"A\", \"source\":4,     \"startTime\":0 }", // annotation
      "{\"batch\": 3, \"data\":\"DEF\", \"endTime\":10, \"annotation\":\"1\", \"annotator\":\"A\", \"source\":4,     \"startTime\":0 }", // annotation to same doc
      
      // "five" has 1 annotation
      "{\"batch\": 4, \"data\":\"HIJ\", \"endTime\":11,                                            \"source\":\"five\", \"startTime\":0 }", // bare doc
      "{\"batch\": 5, \"data\":\"HIJ\", \"endTime\":12, \"annotation\":\"1\", \"annotator\":\"C\", \"source\":\"five\", \"startTime\":0 }", // annotation to same doc
      
      // "six" has nothing
      "{\"batch\": 6, \"data\":\"HIJ\", \"endTime\":13,                                           \"source\":\"six\", \"startTime\":0 }", // bare doc
      "{\"batch\": 7, \"data\":\"HIJ\", \"endTime\":14,                                           \"source\":\"six\", \"startTime\":0 }", // bare doc (redundant with previous)
      
      // "7" has 1 annotation
      "{\"batch\": 0, \"data\":\"KLM\", \"endTime\":15, \"annotation\":\"0\", \"annotator\":\"A\", \"source\":7,     \"startTime\":0 }", // annotation
      
      // "8" has 1 annotation
      "{\"batch\": 0, \"data\":\"NOP\", \"endTime\":16, \"annotation\":\"0\", \"annotator\":\"A\", \"source\":8,     \"startTime\":0 }" // annotation
    );
    Random rand = new Random(seed);
    Collections.shuffle(jsonInstances, rand); // try different orderings
    return "[ \n"+ Joiner.on(", \n").join(jsonInstances) +"]";
    
  }

  private static void assertAllLabeledDataAnnotated(Dataset data){
	Dataset labeledData = Datasets.divideLabeledFromUnlabeled(data).getFirst();
    for (DatasetInstance inst: labeledData){
      Assertions.assertThat(inst.getInfo().getNumAnnotations()).isGreaterThan(0);
    }
  }

  /**
   * Test method for {@link edu.byu.nlp.data.pipes.StopWordRemover#apply(java.util.List)}.
   * @throws FileNotFoundException 
   */
  @Test
  public void testBuildDataset() throws FileNotFoundException {
    Dataset dataset = JSONDocumentTest.buildTestDatasetFromJson(jsonInstances(System.currentTimeMillis()));
    Assertions.assertThat(dataset.getInfo().getNumLabeledDocuments()).isEqualTo(4);
    Assertions.assertThat(dataset.getInfo().getNumUnlabeledDocuments()).isEqualTo(4);
    Assertions.assertThat(dataset.getInfo().getNumDocuments()).isEqualTo(8);
    
    Pair<? extends Dataset, ? extends Dataset> partitions = Datasets.divideLabeledFromUnlabeled(dataset);
    Dataset labeledData = partitions.getFirst();
    Dataset unlabeledData = partitions.getSecond();
    
    // check labeled data
    Assertions.assertThat(labeledData.getInfo().getNumDocuments()).isEqualTo(4);
    for (DatasetInstance inst: labeledData){
      Assertions.assertThat(Sets.newHashSet("1","2","4","five").contains(inst.getInfo().getSource()));
      Assertions.assertThat(
          (inst.getInfo().getSource().equals("1") && inst.getInfo().getNumAnnotations()==2) ||
          (inst.getInfo().getSource().equals("2") && inst.getInfo().getNumAnnotations()==2) ||
          (inst.getInfo().getSource().equals("4") && inst.getInfo().getNumAnnotations()==2) ||
          (inst.getInfo().getSource().equals("five") && inst.getInfo().getNumAnnotations()==1)  
          );
      Assertions.assertThat(inst.getLabel()).isNotEqualTo(dataset.getInfo().getLabelIndexer().indexOf(null));
    }
    // check unlabeled data
    Assertions.assertThat(unlabeledData.getInfo().getNumDocuments()).isEqualTo(4);
    for (DatasetInstance inst: unlabeledData){
      Assertions.assertThat(Sets.newHashSet("3","six","7","8").contains(inst.getInfo().getSource()));
      Assertions.assertThat(
          (inst.getInfo().getSource().equals("3") && inst.getInfo().getNumAnnotations()==0) ||
          (inst.getInfo().getSource().equals("six") && inst.getInfo().getNumAnnotations()==0) ||
          (inst.getInfo().getSource().equals("7") && inst.getInfo().getNumAnnotations()==1) ||
          (inst.getInfo().getSource().equals("8") && inst.getInfo().getNumAnnotations()==1)   
          );
      
      Assertions.assertThat(inst.asFeatureVector().sum()).isEqualTo(1);
      Assertions.assertThat(inst.getLabel()).isEqualTo(dataset.getInfo().getLabelIndexer().indexOf(null));
      
    }
    
  }

  @Test
  public void testHideLabelsByClass1() throws FileNotFoundException{
    int numObservedLabelsPerClass = 1;
    Dataset data = JSONDocumentTest.buildTestDatasetFromJson(jsonInstances(System.currentTimeMillis()));
    data = Datasets.hideAllLabelsButNPerClass(data, numObservedLabelsPerClass, new MersenneTwister(System.currentTimeMillis()));
    
    Dataset labeledData = Datasets.divideLabeledFromUnlabeled(data).getFirst();
    Dataset unlabeledData = Datasets.divideLabeledFromUnlabeled(data).getSecond();
    
    // 2 trusted labels (one per class) will remain unhidden
    Assertions.assertThat(labeledData.getInfo().getNumDocuments()).isEqualTo(2);
    Assertions.assertThat(data.getInfo().getNumLabeledDocuments()).isEqualTo(2);
    Assertions.assertThat(unlabeledData.getInfo().getNumDocuments()).isEqualTo(6);
    Assertions.assertThat(data.getInfo().getNumUnlabeledDocuments()).isEqualTo(6);
    Assertions.assertThat(data.getInfo().getNumDocuments()).isEqualTo(8);
    assertAllLabeledDataAnnotated(data);
  }

  @Test
  public void testHideLabelsByClass2() throws FileNotFoundException{
    int numObservedLabelsPerClass = 2;
    Dataset data = JSONDocumentTest.buildTestDatasetFromJson(jsonInstances(System.currentTimeMillis()));
    data = Datasets.hideAllLabelsButNPerClass(data, numObservedLabelsPerClass, new MersenneTwister(System.currentTimeMillis()));

    // 4 trusted labels (2 per class) will remain unhidden
    Assertions.assertThat(data.getInfo().getNumLabeledDocuments()).isEqualTo(4);
    Assertions.assertThat(data.getInfo().getNumUnlabeledDocuments()).isEqualTo(8-4);
    Assertions.assertThat(data.getInfo().getNumDocuments()).isEqualTo(8);
  }

  @Test
  public void testToFeatureArrayVsToSparseFeatureArray() throws FileNotFoundException{
	// run this test on a variety of test datasets
    Dataset data1 = JSONDocumentTest.buildTestDatasetFromJson(jsonInstances(System.currentTimeMillis()));
    Dataset data2 = DatasetsTestUtil.mockDataset();
    
    for (Dataset data: new Dataset[]{data1,data2}){

      double[][] dense = Datasets.toFeatureArray(data);
      List<Map<Integer, Double>> sparse = Datasets.toSparseFeatureArray(data);
      
      for (int i=0; i<dense.length; i++){
        for (int f=0; f<dense[i].length; f++){
          double denseval = dense[i][f];
          double sparseval = (sparse.get(i).containsKey(f))? sparse.get(i).get(f): 0;
          Assertions.assertThat(sparseval).isEqualTo(denseval);
        }
      }
    
    }
    
  }
  
  @Test
  public void testToFeatureArray() throws FileNotFoundException{
    Dataset data = JSONDocumentTest.buildTestDatasetFromJson(jsonInstances(System.currentTimeMillis()));
    for (DatasetInstance inst: data){
      Assertions.assertThat(
            (inst.getInfo().getSource().equals("1") && DoubleArrays.equals(
                Datasets.toFeatureArray(inst, 5), 
                new double[]{1,0,0,0,0}, 1e-6)) ||
            (inst.getInfo().getSource().equals("2") && DoubleArrays.equals(
                Datasets.toFeatureArray(inst, 5), 
                new double[]{0,1,0,0,0}, 1e-6)) ||
            (inst.getInfo().getSource().equals("3") && DoubleArrays.equals(
                Datasets.toFeatureArray(inst, 5), 
                new double[]{0,1,0,0,0}, 1e-6)) ||
            (inst.getInfo().getSource().equals("4") && DoubleArrays.equals(
                Datasets.toFeatureArray(inst, 5), 
                new double[]{0,1,0,0,0}, 1e-6)) ||
            (inst.getInfo().getSource().equals("five") && DoubleArrays.equals(
                Datasets.toFeatureArray(inst, 5), 
                new double[]{0,0,1,0,0}, 1e-6)) ||
            (inst.getInfo().getSource().equals("six") && DoubleArrays.equals(
                Datasets.toFeatureArray(inst, 5), 
                new double[]{0,0,1,0,0}, 1e-6)) ||
            (inst.getInfo().getSource().equals("7") && DoubleArrays.equals(
                Datasets.toFeatureArray(inst, 5), 
                new double[]{0,0,0,1,0}, 1e-6)) ||
            (inst.getInfo().getSource().equals("8") && DoubleArrays.equals(
                Datasets.toFeatureArray(inst, 5), 
                new double[]{0,0,0,0,1}, 1e-6)) 
          );
      
    }
    
  }
  
}
