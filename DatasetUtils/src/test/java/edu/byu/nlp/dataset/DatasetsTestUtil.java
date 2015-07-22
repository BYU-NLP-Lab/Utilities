package edu.byu.nlp.dataset;

import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import edu.byu.nlp.data.streams.DataStream;
import edu.byu.nlp.data.streams.DataStreams;
import edu.byu.nlp.data.streams.FieldIndexer;
import edu.byu.nlp.data.streams.IndexerCalculator;
import edu.byu.nlp.data.types.DataStreamInstance;
import edu.byu.nlp.data.types.Dataset;
import edu.byu.nlp.data.types.SparseFeatureVector;
import edu.byu.nlp.data.types.SparseFeatureVector.EntryVisitor;
import edu.byu.nlp.util.Indexer;
import edu.byu.nlp.util.Indexers;

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

		List<Map<String,Object>> rawData = Lists.newArrayList();
		SparseFeatureVector data1 = new BasicSparseFeatureVector(new int[]{0,1,2,4}, new double[]{8,4,2,1});
		SparseFeatureVector data2 = new BasicSparseFeatureVector(new int[]{0,2,3,4}, new double[]{18,14,12,1});
		SparseFeatureVector data3 = new BasicSparseFeatureVector(new int[]{0,1,2,3,4}, new double[]{0,1,28,24,22});

		// add labels 
		rawData.add(DataStreamInstance.fromLabelRaw("dummy source 1",null, data1, "ClassA", false));
		rawData.add(DataStreamInstance.fromLabelRaw("dummy source 2",null, data2, "ClassB", false));
		rawData.add(DataStreamInstance.fromLabelRaw("dummy source 3",null, data3, "ClassA", false));
		
		// add annotations
    rawData.add(DataStreamInstance.fromAnnotationRaw("dummy source 1",null, "john", "ClassA", 0L, 1L, null));
    rawData.add(DataStreamInstance.fromAnnotationRaw("dummy source 1",null, "john", "ClassB", 1L, 2L, null));
    rawData.add(DataStreamInstance.fromAnnotationRaw("dummy source 2",null, "john", "ClassB", 2L, 3L, null));
    rawData.add(DataStreamInstance.fromAnnotationRaw("dummy source 3",null, "penny", "ClassA", 4L, 5L, null));
		
		// populate indices
		final Indexer<String> featureIndex = new Indexer<String>();
		Indexer<String> labelIndex = new Indexer<String>();
		Indexer<String> instanceIdIndex = new Indexer<String>();
		Indexer<String> annotatorIdIndex = new Indexer<String>();
		for (Map<String,Object> instance: rawData){
			instanceIdIndex.add((String)DataStreamInstance.getInstanceId(instance));
			labelIndex.add((String)DataStreamInstance.getLabel(instance));
			if (DataStreamInstance.isAnnotation(instance)){
				annotatorIdIndex.add((String)DataStreamInstance.getAnnotator(instance));
			}
			if (DataStreamInstance.getData(instance)!=null){
			  ((SparseFeatureVector)DataStreamInstance.getData(instance)).visitSparseEntries(new EntryVisitor() {
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
    for (Map<String,Object> instance: rawData){
			instanceIdIndex.add((String)DataStreamInstance.getInstanceId(instance));
			labelIndex.add((String)DataStreamInstance.getLabel(instance));
			if (DataStreamInstance.isAnnotation(instance)){
				annotatorIdIndex.add((String)DataStreamInstance.getAnnotator(instance));
			}
			if (DataStreamInstance.getData(instance)!=null){
			  ((SparseFeatureVector)DataStreamInstance.getData(instance)).visitSparseEntries(new EntryVisitor() {
					@Override
					public void visitEntry(int index, double value) {
						featureIndex.add(""+index);
					}
				});
			}
		}
	
		// sparse (all index values between 0 and max are used at least once)
    labelIndex = Indexers.removeNullLabel(labelIndex);
		FieldIndexer<String> labelFieldIndexer = new FieldIndexer<String>(labelIndex);
		Function<String,Integer> annotatorIdFieldIndexer = new FieldIndexer<String>(annotatorIdIndex);
		Function<String,Integer> instanceFieldIndexer = new FieldIndexer<String>(instanceIdIndex);
		
		DataStream stream = DataStream.withSource("data source", rawData)
    .transform(DataStreams.Transforms.transformFieldValue(DataStreamInstance.LABEL, labelFieldIndexer))
    .transform(DataStreams.Transforms.transformFieldValue(DataStreamInstance.ANNOTATION, labelFieldIndexer))
    .transform(DataStreams.Transforms.transformFieldValue(DataStreamInstance.ANNOTATOR, annotatorIdFieldIndexer))
    .transform(DataStreams.Transforms.renameField(DataStreamInstance.INSTANCE_ID, DataStreamInstance.SOURCE))
    .transform(DataStreams.Transforms.transformFieldValue(DataStreamInstance.SOURCE, DataStreamInstance.INSTANCE_ID, instanceFieldIndexer))
    ;
		
		return Datasets.convert("data source", stream, new IndexerCalculator<>(featureIndex, labelIndex, instanceIdIndex, annotatorIdIndex), true);
	}
	
	
	
}
