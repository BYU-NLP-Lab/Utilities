package edu.byu.nlp.data.util;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import edu.byu.nlp.data.types.Dataset;
import edu.byu.nlp.data.types.DatasetInstance;
import edu.byu.nlp.data.types.SparseFeatureVector;
import edu.byu.nlp.data.types.SparseFeatureVector.EntryVisitor;
import edu.byu.nlp.dataset.BasicDataset;
import edu.byu.nlp.dataset.BasicDatasetInstance;
import edu.byu.nlp.dataset.BasicSparseFeatureVector;
import edu.byu.nlp.util.Indexer;
import edu.byu.nlp.util.MaxTracker;

public class DatasetMocker {

	// instances
	List<DatasetInstance> instances = Lists.newArrayList();
	Indexer<String> labelIndexer = new Indexer<String>();
	MaxTracker maxLabelTracker = new MaxTracker();
	MaxTracker maxFeatureTracker = new MaxTracker();
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
		maxLabelTracker.offerLong(label);
		// how many features?
		fv.visitSparseEntries(new EntryVisitor() {
			@Override
			public void visitEntry(int index, double value) {
				maxFeatureTracker.offerLong(index);
			}
		});
		// create instance
		long instanceId = instances.size();
		instances.add(new BasicDatasetInstance(fv, label, instanceId, source, labelIndexer));
		return this;
	}

	public Dataset build(){

		// Populate Identity Indexers
		Indexer<Long> instanceIdIndexer = new Indexer<Long>();
		for (long i = 0; i < instances.size(); i++) {
			instanceIdIndexer.add(i);
		}
		Indexer<String> featureIndexer = new Indexer<String>();
		for (long f = 0; f < maxFeatureTracker.maxLong(); f++) {
			featureIndexer.add("" + f);
		}
		Indexer<String> labelIndexer = new Indexer<String>();
		for (long l = 0; l < maxLabelTracker.maxLong(); l++) {
			labelIndexer.add("" + l);
		}
		Indexer<Long> annotatorIdIndexer = new Indexer<Long>();
		for (long a=0; a<numAnnotators; a++){
			annotatorIdIndexer.add(a);
		}

		// Create Dataset
		return new BasicDataset("", instances, annotatorIdIndexer, featureIndexer, labelIndexer,
				instanceIdIndexer);
	}
	

}
