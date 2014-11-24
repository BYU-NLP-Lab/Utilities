package edu.byu.nlp.dataset;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Lists;

import edu.byu.nlp.annotationinterface.java.AnnotationInterfaceJavaUtils;
import edu.byu.nlp.data.FlatAnnotatedInstance;
import edu.byu.nlp.data.FlatInstance;
import edu.byu.nlp.data.FlatLabeledInstance;
import edu.byu.nlp.data.pipes.FieldIndexer;
import edu.byu.nlp.data.pipes.LabeledInstancePipe;
import edu.byu.nlp.data.pipes.Pipes;
import edu.byu.nlp.data.types.Dataset;
import edu.byu.nlp.data.types.SparseFeatureVector;
import edu.byu.nlp.data.types.SparseFeatureVector.EntryVisitor;
import edu.byu.nlp.dataset.BasicSparseFeatureVector;
import edu.byu.nlp.dataset.Datasets;
import edu.byu.nlp.util.Indexer;

public class DatasetsTestUtil {

//	public static AnnotationSet mockAnnotations(){
//		int numAnnotators = 5;
//		int numFeatures = 3;
//		SparseRealMatrix labelAnnotations = new OpenMapRealMatrix(numAnnotators, numFeatures);
//		labelAnnotations.setEntry(1, 0, 1);
//		labelAnnotations.setEntry(2, 0, 1);
//		labelAnnotations.setEntry(2, 1, 1);
//		labelAnnotations.setEntry(5, 0, 2);
//		
//		SparseRealVector regressandMeans = null; 
//		SparseRealVector regressandVariances = null;
//		
//		return new BasicAnnotationSet(labelAnnotations, regressandMeans, regressandVariances);
//		
//	}
	
//	public static DatasetInstance mockDatasetInstance(int label, double regressand){
//		
//		int[] indices = {1,2,3,5};
//		double[] values = {3,7,11,17};
//		BasicSparseFeatureVector vector = new BasicSparseFeatureVector(indices, values);
//		
//		String source = "src";
//		return new BasicDatasetInstance(vector, label, regressand, mockAnnotations(), source);
//	}
	
	public static Dataset mockDataset(){

		List<FlatInstance<SparseFeatureVector, String>> labels = Lists.newArrayList();

		// add data 
		SparseFeatureVector data1 = new BasicSparseFeatureVector(
				new int[]{0,1,2,4}, new double[]{8,4,2,1});
		labels.add(new FlatLabeledInstance<SparseFeatureVector, String>(
				AnnotationInterfaceJavaUtils.newLabeledInstance(data1, "ClassA", "dummy source 1")));

		// add data 
		SparseFeatureVector data2 = new BasicSparseFeatureVector(
				new int[]{0,2,3,4}, new double[]{18,14,12,1});
		labels.add(new FlatLabeledInstance<SparseFeatureVector, String>(
				AnnotationInterfaceJavaUtils.newLabeledInstance(data2, "ClassB", "dummy source 2")));

		// add data 
		SparseFeatureVector data3 = new BasicSparseFeatureVector(
				new int[]{0,1,2,3,4}, new double[]{0,1,28,24,22});
		labels.add(new FlatLabeledInstance<SparseFeatureVector, String>(
				AnnotationInterfaceJavaUtils.newLabeledInstance(data3, "ClassA", "dummy source 3")));

		
		// add annotation
		labels.add(new FlatAnnotatedInstance<SparseFeatureVector, String>(
				AnnotationInterfaceJavaUtils.<SparseFeatureVector, String>
				newAnnotatedInstance("john", "ClassA", "dummy source 1", null)));

		// add annotation
		labels.add(new FlatAnnotatedInstance<SparseFeatureVector, String>(
				AnnotationInterfaceJavaUtils.<SparseFeatureVector, String>
				newAnnotatedInstance("john", "ClassB", "dummy source 1", null)));

		// add annotation
		labels.add(new FlatAnnotatedInstance<SparseFeatureVector, String>(
				AnnotationInterfaceJavaUtils.<SparseFeatureVector, String>
				newAnnotatedInstance("john", "ClassB", "dummy source 2", null)));
		
		// add annotation
		labels.add(new FlatAnnotatedInstance<SparseFeatureVector, String>(
				AnnotationInterfaceJavaUtils.<SparseFeatureVector, String>
				newAnnotatedInstance("penny", "ClassA", "dummy source 3", null)));
		
		// populate indices
		String datasetSource = "dummy source";
		final Indexer<String> featureIndex = new Indexer<String>();
		Indexer<String> labelIndex = new Indexer<String>();
		Indexer<Long> instanceIdIndex = new Indexer<Long>();
		Indexer<Long> annotatorIdIndex = new Indexer<Long>();
		for (FlatInstance<SparseFeatureVector, String> label: labels){
			instanceIdIndex.add(label.getInstanceId());
			labelIndex.add(label.getLabel());
			if (label.isAnnotation()){
				annotatorIdIndex.add(label.getAnnotator());
			}
			if (label.getData()!=null){
				label.getData().visitSparseEntries(new EntryVisitor() {
					@Override
					public void visitEntry(int index, double value) {
						featureIndex.add(""+index);
					}
				});
			}
		}
		
		// translate FlatInstance with indexers
		// (except for the featureIndex--we constructed the data above so that it would 
		// already be "indexed" (index values are integers, start with 0, and are not 
		for (FlatInstance<SparseFeatureVector, String> label: labels){
			instanceIdIndex.add(label.getInstanceId());
			labelIndex.add(label.getLabel());
			if (label.isAnnotation()){
				annotatorIdIndex.add(label.getAnnotator());
			}
			if (label.getData()!=null){
				label.getData().visitSparseEntries(new EntryVisitor() {
					@Override
					public void visitEntry(int index, double value) {
						featureIndex.add(""+index);
					}
				});
			}
		}
	
		// sparse (all index values between 0 and max are used at least once)
		FieldIndexer<String> labelFieldIndexer = new FieldIndexer<String>(labelIndex);
		Function<Long,Long> annotatorIdFieldIndexer = FieldIndexer.cast2Long(new FieldIndexer<Long>(annotatorIdIndex));
		Function<Long,Long> instanceFieldIndexer = FieldIndexer.cast2Long(new FieldIndexer<Long>(instanceIdIndex));
		LabeledInstancePipe<SparseFeatureVector, String, SparseFeatureVector, Integer> indexingPipe = Pipes.<SparseFeatureVector,String,SparseFeatureVector,Integer>labeledInstanceTransformingPipe( 
				Functions.<SparseFeatureVector>identity(), 
				labelFieldIndexer, 
				Functions.<String>identity(), 
				instanceFieldIndexer, 
				annotatorIdFieldIndexer);
		Iterable<FlatInstance<SparseFeatureVector, Integer>> indexTransformedInstances = indexingPipe.apply(labels);
		
		return Datasets.convert(datasetSource, indexTransformedInstances, featureIndex, labelIndex, instanceIdIndex, annotatorIdIndex, true);
	}
	
	
}
