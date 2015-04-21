package edu.byu.nlp.dataset;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.random.MersenneTwister;
import org.fest.assertions.Assertions;
import org.fest.assertions.Fail;
import org.junit.Test;

import com.google.common.collect.Sets;

import edu.byu.nlp.data.FlatInstance;
import edu.byu.nlp.data.types.Dataset;
import edu.byu.nlp.data.types.DatasetInstance;
import edu.byu.nlp.data.types.SparseFeatureVector;
import edu.byu.nlp.data.util.JsonDatasetMocker;
import edu.byu.nlp.util.Counter;
import edu.byu.nlp.util.Counters;
import edu.byu.nlp.util.DoubleArrays;
import edu.byu.nlp.util.IntArrays;
import edu.byu.nlp.util.Pair;

public class DatasetsTest {


	  private static void assertAllLabeledDataAnnotated(Dataset data){
		Dataset labeledData = Datasets.divideInstancesWithObservedLabels(data).getFirst();
	    for (DatasetInstance inst: labeledData){
	      Assertions.assertThat(inst.getInfo().getNumAnnotations()).isGreaterThan(0);
	    }
	  }

	  /**
	   * Test method for {@link edu.byu.nlp.data.pipes.StopWordRemover#apply(java.util.List)}.
	 * @throws IOException 
	   */
	  @Test
	  public void testBuildDataset() throws IOException {
	    Dataset dataset = JsonDatasetMocker.buildTestDatasetFromJson(JsonDatasetMocker.jsonInstances2(System.currentTimeMillis()));
	    Assertions.assertThat(dataset.getInfo().getNumDocumentsWithObservedLabels()).isEqualTo(4);
	    Assertions.assertThat(dataset.getInfo().getNumDocumentsWithoutObservedLabels()).isEqualTo(4);
	    Assertions.assertThat(dataset.getInfo().getNumDocuments()).isEqualTo(8);
	    
	    Pair<? extends Dataset, ? extends Dataset> partitions = Datasets.divideInstancesWithObservedLabels(dataset);
	    Dataset labeledData = partitions.getFirst();
	    Dataset unlabeledData = partitions.getSecond();
	    
	    // check labeled data
	    Assertions.assertThat(labeledData.getInfo().getNumDocuments()).isEqualTo(4);
	    for (DatasetInstance inst: labeledData){
	      Assertions.assertThat(Sets.newHashSet("1","2","3","4")).contains(inst.getInfo().getSource());
	      Assertions.assertThat(
	          (inst.getInfo().getSource().equals("1") && inst.getInfo().getNumAnnotations()==2) ||
	          (inst.getInfo().getSource().equals("2") && inst.getInfo().getNumAnnotations()==2) ||
	          (inst.getInfo().getSource().equals("3") && inst.getInfo().getNumAnnotations()==0) ||
	          (inst.getInfo().getSource().equals("4") && inst.getInfo().getNumAnnotations()==2) 
	          ).isTrue();
	      Assertions.assertThat(dataset.getInfo().getNullLabel() == dataset.getInfo().getLabelIndexer().indexOf(null));
	      Assertions.assertThat(inst.getObservedLabel()).isNotEqualTo(dataset.getInfo().getLabelIndexer().indexOf(null));
	      Assertions.assertThat(inst.hasObservedLabel()).isTrue();
	      Assertions.assertThat(inst.getLabel()).isNotEqualTo(dataset.getInfo().getLabelIndexer().indexOf(null));
	      Assertions.assertThat(inst.hasLabel()).isTrue();
	    }
	    // check unlabeled data
	    Assertions.assertThat(unlabeledData.getInfo().getNumDocuments()).isEqualTo(4);
	    for (DatasetInstance inst: unlabeledData){
	      Assertions.assertThat(Sets.newHashSet("five","six","7","8")).contains(inst.getInfo().getSource());
	      Assertions.assertThat(
	          (inst.getInfo().getSource().equals("five") && inst.getInfo().getNumAnnotations()==1) ||
	          (inst.getInfo().getSource().equals("six") && inst.getInfo().getNumAnnotations()==0) ||
	          (inst.getInfo().getSource().equals("7") && inst.getInfo().getNumAnnotations()==1) ||
	          (inst.getInfo().getSource().equals("8") && inst.getInfo().getNumAnnotations()==1)   
	          ).isTrue();
	      
	      Assertions.assertThat(inst.asFeatureVector().sum()).isEqualTo(1);
	      Assertions.assertThat(inst.hasObservedLabel()).isFalse(); 
	      
	    }
	    
	  }

	  @Test
	  public void testHideLabelsByClass1() throws IOException{
	    int numObservedLabelsPerClass = 1;
	    Dataset data = JsonDatasetMocker.buildTestDatasetFromJson(JsonDatasetMocker.jsonInstances2(System.currentTimeMillis()));
	    data = Datasets.hideAllLabelsButNPerClass(data, numObservedLabelsPerClass, new MersenneTwister(System.currentTimeMillis()));
	    
	    Dataset labeledData = Datasets.divideInstancesWithObservedLabels(data).getFirst();
	    Dataset unlabeledData = Datasets.divideInstancesWithObservedLabels(data).getSecond();
	    
	    // 2 trusted labels (one per class) will remain unhidden
	    Assertions.assertThat(labeledData.getInfo().getNumDocuments()).isEqualTo(2);
	    Assertions.assertThat(data.getInfo().getNumDocumentsWithObservedLabels()).isEqualTo(2);
	    Assertions.assertThat(unlabeledData.getInfo().getNumDocuments()).isEqualTo(6);
	    Assertions.assertThat(data.getInfo().getNumDocumentsWithoutObservedLabels()).isEqualTo(6);
	    Assertions.assertThat(data.getInfo().getNumDocuments()).isEqualTo(8);
	    assertAllLabeledDataAnnotated(data);
	  }

	  @Test
	  public void testHideLabelsByClass2() throws IOException{
	    int numObservedLabelsPerClass = 2;
	    Dataset data = JsonDatasetMocker.buildTestDatasetFromJson(JsonDatasetMocker.jsonInstances2(System.currentTimeMillis()));
	    data = Datasets.hideAllLabelsButNPerClass(data, numObservedLabelsPerClass, new MersenneTwister(System.currentTimeMillis()));

	    // 4 trusted labels (2 per class) will remain unhidden
	    Assertions.assertThat(data.getInfo().getNumDocumentsWithObservedLabels()).isEqualTo(4);
	    Assertions.assertThat(data.getInfo().getNumDocumentsWithoutObservedLabels()).isEqualTo(8-4);
	    Assertions.assertThat(data.getInfo().getNumDocuments()).isEqualTo(8);
	  }

	  @Test
	  public void testToFeatureArrayVsToSparseFeatureArray() throws IOException{
		// run this test on a variety of test datasets
	    Dataset data1 = JsonDatasetMocker.buildTestDatasetFromJson(JsonDatasetMocker.jsonInstances2(System.currentTimeMillis()));
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
	  public void testToFeatureArray() throws IOException{
	    Dataset data = JsonDatasetMocker.buildTestDatasetFromJson(JsonDatasetMocker.jsonInstances2(System.currentTimeMillis()));
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
	  
	  @Test
	  public void testAddAnnotation() throws IOException{
		// base dataset
	    Dataset dataset = JsonDatasetMocker.buildTestDatasetFromJson(JsonDatasetMocker.jsonInstances2(System.currentTimeMillis()));
	    // annotations
	    List<FlatInstance<SparseFeatureVector, Integer>> annotations = Datasets.annotationsIn(JsonDatasetMocker.buildTestDatasetFromJson(JsonDatasetMocker.jsonInstances3(System.currentTimeMillis())));
	    // add them
	    Datasets.addAnnotationsToDataset(dataset, annotations);
	    
	    // labeled vs unlabeled shouldn't change
	    Assertions.assertThat(dataset.getInfo().getNumDocumentsWithObservedLabels()).isEqualTo(4);
	    Assertions.assertThat(dataset.getInfo().getNumDocumentsWithoutObservedLabels()).isEqualTo(4);
	    Assertions.assertThat(dataset.getInfo().getNumDocuments()).isEqualTo(8);
	    
	    Pair<? extends Dataset, ? extends Dataset> partitions = Datasets.divideInstancesWithObservedLabels(dataset);
	    Dataset labeledData = partitions.getFirst();
	    Dataset unlabeledData = partitions.getSecond();
	    
	    // check labeled data
	    Assertions.assertThat(labeledData.getInfo().getNumDocuments()).isEqualTo(4);
	    for (DatasetInstance inst: labeledData){
	      Assertions.assertThat(Sets.newHashSet("1","2","3","4")).contains(inst.getInfo().getSource());
	      Assertions.assertThat(
	          (inst.getInfo().getSource().equals("1") && inst.getInfo().getNumAnnotations()==3) ||
	          (inst.getInfo().getSource().equals("2") && inst.getInfo().getNumAnnotations()==3) ||
	          (inst.getInfo().getSource().equals("3") && inst.getInfo().getNumAnnotations()==0) ||
	          (inst.getInfo().getSource().equals("4") && inst.getInfo().getNumAnnotations()==3) 
	          ).isTrue();
	      Assertions.assertThat(inst.getObservedLabel()).isNotEqualTo(dataset.getInfo().getLabelIndexer().indexOf(null));
	      Assertions.assertThat(inst.hasObservedLabel()).isTrue();
	      Assertions.assertThat(inst.getLabel()).isNotEqualTo(dataset.getInfo().getLabelIndexer().indexOf(null));
	      Assertions.assertThat(inst.hasLabel()).isTrue();
	    }
	    // check unlabeled data
	    Assertions.assertThat(unlabeledData.getInfo().getNumDocuments()).isEqualTo(4);
	    for (DatasetInstance inst: unlabeledData){
	      Assertions.assertThat(Sets.newHashSet("five","six","7","8")).contains(inst.getInfo().getSource());
	      Assertions.assertThat(
	          (inst.getInfo().getSource().equals("five") && inst.getInfo().getNumAnnotations()==1) ||
	          (inst.getInfo().getSource().equals("six") && inst.getInfo().getNumAnnotations()==3) ||
	          (inst.getInfo().getSource().equals("7") && inst.getInfo().getNumAnnotations()==1) ||
	          (inst.getInfo().getSource().equals("8") && inst.getInfo().getNumAnnotations()==1)   
	          ).isTrue();;
	      
	      Assertions.assertThat(inst.asFeatureVector().sum()).isEqualTo(1);
	      Assertions.assertThat(!inst.hasObservedLabel());
	      
	    }
	  }
	
	
	
	@Test
	public void testSplitbySizeException(){
		Dataset dataset = DatasetsTestUtil.mockDataset();
		try{
			Datasets.split(dataset, new int[]{1,1,2});
			Fail.fail("should have failed");
		}
		catch (Exception e){
			// ignore
		}
	}
	
	@Test
	public void testSplitbySize1(){
		Dataset dataset = DatasetsTestUtil.mockDataset();
		
		List<Dataset> splits = Datasets.split(dataset, new int[]{1,1,1});
		Assertions.assertThat(splits.size()).isEqualTo(3);
		
		Dataset ds1 = splits.get(0);
		Dataset ds2 = splits.get(1);
		Dataset ds3 = splits.get(2);

		Assertions.assertThat(ds1.getInfo().getNumDocuments()).isEqualTo(1);
		Assertions.assertThat(ds2.getInfo().getNumDocuments()).isEqualTo(1);
		Assertions.assertThat(ds3.getInfo().getNumDocuments()).isEqualTo(1);
	}

	@Test
	public void testSplitbySize2(){
		Dataset dataset = DatasetsTestUtil.mockDataset();
		
		List<Dataset> splits = Datasets.split(dataset, new int[]{2,1});
		Assertions.assertThat(splits.size()).isEqualTo(2);
		
		Dataset ds1 = splits.get(0);
		Dataset ds2 = splits.get(1);

		Assertions.assertThat(ds1.getInfo().getNumDocuments()).isEqualTo(2);
		Assertions.assertThat(ds2.getInfo().getNumDocuments()).isEqualTo(1);
	}

	@Test
	public void testSplitbyPercentException(){
		Dataset dataset = DatasetsTestUtil.mockDataset();
		try{
			Datasets.split(dataset, new double[]{10,10,20});
			Fail.fail("should have failed");
		}
		catch (Exception e){
			// ignore
		}
	}
	
	@Test
	public void testSplitbyPercent(){
		Dataset dataset = DatasetsTestUtil.mockDataset();
		
		List<Dataset> splits = Datasets.split(dataset, new double[]{50,50});
		Assertions.assertThat(splits.size()).isEqualTo(2);
		
		Dataset ds1 = splits.get(0);
		Dataset ds2 = splits.get(1);

		// floor(3*.5)=1. The first will get an extra count to make up difference.
		Assertions.assertThat(ds1.getInfo().getNumDocuments()).isEqualTo(2);
		Assertions.assertThat(ds2.getInfo().getNumDocuments()).isEqualTo(1);
	}
	
	@Test
	public void testFeatureVectors2FeatureSequences(){
		Dataset data = DatasetsTestUtil.mockDataset();
		
		int[][] docs = Datasets.featureVectors2FeatureSequences(data);
		
		// doc 0 (0:8, 1:4, 2:2, 4:1)
		Assertions.assertThat(docs[0]).contains(0,1,2,4);
		Counter<Integer> featureCountsInDoc = Counters.count(IntArrays.asList(docs[0]));
		Assertions.assertThat(featureCountsInDoc.getCount(0)).isEqualTo(8);
		Assertions.assertThat(featureCountsInDoc.getCount(1)).isEqualTo(4);
		Assertions.assertThat(featureCountsInDoc.getCount(2)).isEqualTo(2);
		Assertions.assertThat(featureCountsInDoc.getCount(3)).isEqualTo(0);
		Assertions.assertThat(featureCountsInDoc.getCount(4)).isEqualTo(1);
		Assertions.assertThat(docs[0].length).isEqualTo(15);
	
		// doc 1 (0:18, 2:14, 3:12, 4:1)
		Assertions.assertThat(docs[1]).contains(0,2,3,4);
		featureCountsInDoc = Counters.count(IntArrays.asList(docs[1]));
		Assertions.assertThat(featureCountsInDoc.getCount(0)).isEqualTo(18);
		Assertions.assertThat(featureCountsInDoc.getCount(1)).isEqualTo(0);
		Assertions.assertThat(featureCountsInDoc.getCount(2)).isEqualTo(14);
		Assertions.assertThat(featureCountsInDoc.getCount(3)).isEqualTo(12);
		Assertions.assertThat(featureCountsInDoc.getCount(4)).isEqualTo(1);
		Assertions.assertThat(docs[1].length).isEqualTo(45);
		
		// doc 2 (0:0, 1:1, 2:28, 3:24, 4:22)
		Assertions.assertThat(docs[2]).contains(1,2,3,4);
		featureCountsInDoc = Counters.count(IntArrays.asList(docs[2]));
		Assertions.assertThat(featureCountsInDoc.getCount(0)).isEqualTo(0);
		Assertions.assertThat(featureCountsInDoc.getCount(1)).isEqualTo(1);
		Assertions.assertThat(featureCountsInDoc.getCount(2)).isEqualTo(28);
		Assertions.assertThat(featureCountsInDoc.getCount(3)).isEqualTo(24);
		Assertions.assertThat(featureCountsInDoc.getCount(4)).isEqualTo(22);
		Assertions.assertThat(docs[2].length).isEqualTo(75);

	}
	
}
