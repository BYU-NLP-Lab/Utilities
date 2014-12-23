package edu.byu.nlp.dataset;

import java.util.List;

import org.fest.assertions.Assertions;
import org.fest.assertions.Fail;
import org.junit.Test;

import edu.byu.nlp.data.types.Dataset;
import edu.byu.nlp.util.Counter;
import edu.byu.nlp.util.Counters;
import edu.byu.nlp.util.IntArrays;

public class DatasetsTest {

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
