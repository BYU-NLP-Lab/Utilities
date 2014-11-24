package edu.byu.nlp.dataset;

import java.util.List;

import org.fest.assertions.Assertions;
import org.fest.assertions.Fail;
import org.junit.Test;

import edu.byu.nlp.data.types.Dataset;

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
			Datasets.split(dataset, new double[]{.1,.1,.2});
			Fail.fail("should have failed");
		}
		catch (Exception e){
			// ignore
		}
	}
	
	@Test
	public void testSplitbyPercent(){
		Dataset dataset = DatasetsTestUtil.mockDataset();
		
		List<Dataset> splits = Datasets.split(dataset, new double[]{.5,.5});
		Assertions.assertThat(splits.size()).isEqualTo(2);
		
		Dataset ds1 = splits.get(0);
		Dataset ds2 = splits.get(1);

		// floor(3*.5)=1. The first will get an extra count to make up difference.
		Assertions.assertThat(ds1.getInfo().getNumDocuments()).isEqualTo(2);
		Assertions.assertThat(ds2.getInfo().getNumDocuments()).isEqualTo(1);
	}
	
}
