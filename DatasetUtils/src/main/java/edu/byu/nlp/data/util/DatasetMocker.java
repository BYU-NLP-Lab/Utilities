package edu.byu.nlp.data.util;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import edu.byu.nlp.data.streams.IndexerCalculator;
import edu.byu.nlp.data.types.Dataset;
import edu.byu.nlp.data.types.DatasetInstance;
import edu.byu.nlp.data.types.SparseFeatureVector;
import edu.byu.nlp.data.types.SparseFeatureVector.EntryVisitor;
import edu.byu.nlp.dataset.BasicDataset;
import edu.byu.nlp.dataset.BasicDatasetInstance;
import edu.byu.nlp.dataset.BasicSparseFeatureVector;
import edu.byu.nlp.util.ArgMinMaxTracker.MinMaxTracker;
import edu.byu.nlp.util.Indexer;

public class DatasetMocker {

	// instances
	List<DatasetInstance> instances = Lists.newArrayList();
	Indexer<String> labelIndexer = new Indexer<String>();
	MinMaxTracker<Integer> maxLabelTracker = MinMaxTracker.newMinMaxTracker();
	MinMaxTracker<Integer> maxFeatureTracker = MinMaxTracker.newMinMaxTracker();
	int numAnnotators = -1;

	public DatasetMocker addInstance(String source, double[] denseFeatureValues,
			int label, int[][] annotations) {
		return addInstance(source, BasicSparseFeatureVector.fromDenseFeatureVector(denseFeatureValues), label, annotations);
	}
	
	public DatasetMocker addInstance(String source, int[] sparseFeatureIndices, double[] sparseFeatureValues,
			int label, int[][] annotations) {
		SparseFeatureVector fv = new BasicSparseFeatureVector(sparseFeatureIndices, sparseFeatureValues);
		return addInstance(source, fv, label, annotations);
	}

	public DatasetMocker addInstance(String source, SparseFeatureVector fv, int label, int[][] annotations) {
		// how many annotators? (ensure consistency)
		int tmpNumAnnotators = (annotations==null)? 0: annotations.length;
		if (numAnnotators<0){
			numAnnotators = tmpNumAnnotators;
		}
		else{
			Preconditions.checkArgument(tmpNumAnnotators==numAnnotators);
		}
		// how many labels?
		maxLabelTracker.offer(label);
		// how many features?
		fv.visitSparseEntries(new EntryVisitor() {
			@Override
			public void visitEntry(int index, double value) {
				maxFeatureTracker.offer(index);
			}
		});
		// create instance
		int instanceId = instances.size();
		instances.add(new BasicDatasetInstance(fv, label, instanceId, ""+instanceId, labelIndexer));
		return this;
	}

	public Dataset build(){

		// Populate Identity Indexers
		Indexer<String> instanceIdIndexer = new Indexer<String>();
		for (long i = 0; i < instances.size(); i++) {
			instanceIdIndexer.add(""+i);
		}
		Indexer<String> featureIndexer = new Indexer<String>();
		for (long f = 0; f < maxFeatureTracker.max(); f++) {
			featureIndexer.add("" + f);
		}
		Indexer<String> labelIndexer = new Indexer<String>();
		for (long l = 0; l < maxLabelTracker.max(); l++) {
			labelIndexer.add("" + l);
		}
		Indexer<String> annotatorIdIndexer = new Indexer<String>();
		for (long a=0; a<numAnnotators; a++){
			annotatorIdIndexer.add(""+a);
		}

		// Create Dataset
		return new BasicDataset("", instances, new IndexerCalculator<>(featureIndexer, labelIndexer, instanceIdIndexer, annotatorIdIndexer));
	}
	

}
