package edu.byu.nlp.dataset;

import java.util.List;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.google.common.collect.Lists;

import edu.byu.nlp.annotationinterface.java.AnnotationInterfaceJavaUtils;
import edu.byu.nlp.data.types.AnnotationSet;
import edu.byu.nlp.data.types.Dataset;
import edu.byu.nlp.data.types.DatasetInfo;
import edu.byu.nlp.data.types.DatasetInstance;
import edu.byu.nlp.data.types.SparseFeatureVector;

public class BasicDatasetTest {

	@Test
	public void testDataSums(){
		Dataset dataset = DatasetsTestUtil.mockDataset();
		List<DatasetInstance> instances = Lists.newArrayList(dataset);

		SparseFeatureVector vec1 = instances.get(0).asFeatureVector();
		SparseFeatureVector vec2 = instances.get(1).asFeatureVector();
		SparseFeatureVector vec3 = instances.get(2).asFeatureVector();

		Assertions.assertThat(vec1.sum()).isEqualTo(15);
		Assertions.assertThat(vec2.sum()).isEqualTo(45);
		Assertions.assertThat(vec3.sum()).isEqualTo(75);
	}
	
	@Test
	public void testDatasetInfo(){
		Dataset dataset = DatasetsTestUtil.mockDataset();
		Assertions.assertThat(dataset.getInfo().getAnnotatorIdIndexer().size()).isEqualTo(2);
		Assertions.assertThat(dataset.getInfo().getNumClasses()).isEqualTo(2);
		Assertions.assertThat(dataset.getInfo().getNumDocuments()).isEqualTo(3);
		Assertions.assertThat(dataset.getInfo().getNumFeatures()).isEqualTo(5);
	}
	
	@Test
	public void testAnnotationSets(){
		Dataset dataset = DatasetsTestUtil.mockDataset();
		DatasetInfo info = dataset.getInfo();
		
		int pennyIndex = info.getAnnotatorIdIndexer().indexOf(AnnotationInterfaceJavaUtils.annotatorIdFromUsername("penny"));
		int johnIndex = info.getAnnotatorIdIndexer().indexOf(AnnotationInterfaceJavaUtils.annotatorIdFromUsername("john"));
		int labelAIndex = info.getLabelIndexer().indexOf("ClassA");
		int labelBIndex = info.getLabelIndexer().indexOf("ClassB");
		
		
		List<DatasetInstance> instances = Lists.newArrayList(dataset);
		AnnotationSet a0 = instances.get(0).getAnnotations();

		// john annotated item 0 twice (once with 0 and once with 1)
		Assertions.assertThat(a0.getLabelAnnotations().getEntry(johnIndex, labelAIndex)).isEqualTo(1);
		Assertions.assertThat(a0.getLabelAnnotations().getEntry(johnIndex, labelBIndex)).isEqualTo(1);

		AnnotationSet a1 = instances.get(1).getAnnotations();
		Assertions.assertThat(a1.getLabelAnnotations().getEntry(johnIndex, labelBIndex)).isEqualTo(1);

		AnnotationSet a2 = instances.get(2).getAnnotations();
		Assertions.assertThat(a2.getLabelAnnotations().getEntry(pennyIndex, labelAIndex)).isEqualTo(1);

		Assertions.assertThat(a2.getLabelAnnotations().getEntry(pennyIndex, labelBIndex)).isEqualTo(0);
		Assertions.assertThat(a2.getLabelAnnotations().getEntry(pennyIndex, labelBIndex)).isEqualTo(0);
	}
}
