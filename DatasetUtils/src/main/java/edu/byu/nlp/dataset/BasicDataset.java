package edu.byu.nlp.dataset;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.random.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.byu.nlp.data.types.Dataset;
import edu.byu.nlp.data.types.DatasetInfo;
import edu.byu.nlp.data.types.DatasetInstance;
import edu.byu.nlp.util.Collections3;
import edu.byu.nlp.util.Indexer;

public class BasicDataset implements Dataset {
	private static final Logger logger = LoggerFactory.getLogger(BasicDataset.class);

	private List<DatasetInstance> instances;
	private DatasetInfo info;

	public BasicDataset(Dataset other){
		this.instances = Lists.newArrayList(other);
		this.info = other.getInfo();
	}
	
	/**
	 * This is the constructor you should use if you are creating a Dataset from scratch
	 * (rather than transforming an existing dataset). 
	 * Creates a dataset with info calculated from instances.
	 */
	public BasicDataset(String source, Iterable<DatasetInstance> instances, 
		Indexer<Long> annotatorIdIndexer, Indexer<String> featureIndexer, Indexer<String> labelIndexer, Indexer<Long> instanceIdIndexer){
		this(instances, Datasets.infoWithCalculatedCounts(instances, source, annotatorIdIndexer, featureIndexer, labelIndexer, instanceIdIndexer));
	}

	/**
	 * Creates a dataset with an info from provided stats. This method trusts that you got it right, 
	 * and does not double-check your work.
	 */
	public BasicDataset(String source, Iterable<DatasetInstance> instances, 
			int numDocuments, int numDocumentsWithAnnotations, int numDocumentsWithLabels, int numDocumentsWithObservedLabels, 
		int numTokens, int numTokensWithAnnotations, int numTokensWithLabels, int numTokensWithObservedLabels, 
		Indexer<Long> annotatorIdIndexer, Indexer<String> featureIndexer, Indexer<String> labelIndexer, Indexer<Long> instanceIdIndexer){
		this(instances, new Info(source, 
				numDocuments, numDocumentsWithAnnotations, numDocumentsWithLabels, numDocumentsWithObservedLabels, 
				numTokens, numTokensWithAnnotations, numTokensWithLabels, numTokensWithObservedLabels, annotatorIdIndexer, featureIndexer, labelIndexer, instanceIdIndexer));
	}
	
	/**
	 * Creates a dataset with the given info. Trusts that the stats in the info 
	 * are correct, and does not double-check your work.
	 */
	public BasicDataset(Iterable<DatasetInstance> instances, DatasetInfo info){
		this.instances = Lists.newArrayList(instances);
		this.info=info;
	}

	@Override
	public Iterator<DatasetInstance> iterator() {
		return instances.iterator();
	}

	@Override
	public void shuffle(RandomGenerator rnd) {
		Collections3.shuffle(instances, rnd);
	}

	@Override
	public DatasetInfo getInfo() {
		return info;
	}
	
	@Override
	public String toString() {
		return info.toString();
	}
	
	public static class Info implements DatasetInfo{

		private String source;
		private int numDocuments;
		private int numDocumentsWithAnnotations;
		private int numDocumentsWithLabels;
		private int numDocumentsWithObservedLabels;
		private int numTokens;
		private int numTokensWithAnnotations;
		private int numTokensWithLabels;
		private int numTokensWithObservedLabels;
		private int numFeatures;
		private int numClasses;
		private Indexer<String> featureIndexer;
		private Indexer<String> labelIndexer;
		private Indexer<Long> annotatorIdIndex;
		private Indexer<Long> instanceIdIndexer;

		public Info(String source, 
				int numDocuments, int numDocumentsWithAnnotations, int numDocumentsWithLabels, int numDocumentsWithObservedLabels, 
				int numTokens, int numTokensWithAnnotations, int numTokensWithLabels, int numTokensWithObservedLabels, 
				Indexer<Long> annotatorIdIndex, Indexer<String> featureIndexer, Indexer<String> labelIndexer, Indexer<Long> instanceIdIndexer){
			this.source=source;
			this.numDocuments=numDocuments;
			this.numDocumentsWithAnnotations=numDocumentsWithAnnotations;
			this.numDocumentsWithLabels=numDocumentsWithLabels;
			this.numDocumentsWithObservedLabels=numDocumentsWithObservedLabels;
			this.numTokens=numTokens;
			this.numTokensWithAnnotations=numTokensWithAnnotations;
			this.numTokensWithLabels=numTokensWithLabels;
			this.numTokensWithObservedLabels=numTokensWithObservedLabels;
			this.numFeatures=featureIndexer.size();
			this.numClasses=labelIndexer.size();
			this.featureIndexer=featureIndexer;
			this.labelIndexer=labelIndexer;
			this.annotatorIdIndex=annotatorIdIndex;
			this.instanceIdIndexer=instanceIdIndexer;
		}
		
		@Override
		public String getSource() {
			return source;
		}

		@Override
		public int getNumDocuments() {
			return numDocuments;
		}

		@Override
		public int getNumDocumentsWithAnnotations() {
			return numDocumentsWithAnnotations;
		}

		@Override
		public int getNumDocumentsWithoutAnnotations() {
			return numDocuments - numDocumentsWithAnnotations;
		}

		@Override
		public int getNumDocumentsWithLabels() {
			return numDocumentsWithLabels;
		}

		@Override
		public int getNumDocumentsWithoutLabels() {
			return numDocuments - numDocumentsWithLabels;
		}

		@Override
		public int getNumDocumentsWithObservedLabels() {
			return numDocumentsWithObservedLabels;
		}

		@Override
		public int getNumDocumentsWithoutObservedLabels() {
			return numDocuments-numDocumentsWithObservedLabels;
		}

		@Override
		public int getNumFeatures() {
			return numFeatures;
		}

		@Override
		public int getNumClasses() {
			return numClasses;
		}

		@Override
		public Indexer<String> getLabelIndexer() {
			return labelIndexer;
		}

		@Override
		public Indexer<String> getFeatureIndexer() {
			return featureIndexer;
		}

		@Override
		public Indexer<Long> getAnnotatorIdIndexer() {
			return annotatorIdIndex;
		}

		@Override
		public int getNumTokens() {
			return numTokens;
		}

		@Override
		public int getNumTokensWithAnnotations() {
			return numTokensWithAnnotations;
		}

		@Override
		public int getNumTokensWithoutAnnotations() {
			return numTokens - numTokensWithAnnotations;
		}

		@Override
		public int getNumTokensWithLabels() {
			return numTokensWithLabels;
		}

		@Override
		public int getNumTokensWithoutLabels() {
			return numTokens - numTokensWithLabels;
		}
		
		@Override
		public int getNumTokensWithObservedLabels() {
			return numTokensWithObservedLabels;
		}

		@Override
		public int getNumTokensWithoutObservedLabels() {
			return numTokens - numTokensWithObservedLabels;
		}

		@Override
		public Indexer<Long> getInstanceIdIndexer() {
			return instanceIdIndexer;
		}
		@Override
		public String toString() {
			return "numdocs="+numDocuments+" numtok="+numTokens+" numfeat="+numFeatures+" numclass="+numClasses+" src="+source;
		}

		@Override
		public int getNullLabel() {
			return labelIndexer.indexOf(null);
		}

		@Override
		public int getNumAnnotators() {
			return annotatorIdIndex.size();
		}
		
	}

	/**
	 * Cached instances lookups. The cache is not copied when a dataset is 
	 * copied or combined with another. This leads to some inefficiency by 
	 * allowing us to avoid lots of messy bookkeeping. We never need to 
	 * update our cache since datasets are immutable (at least wrt instances).
	 */
	private Map<String,DatasetInstance> instanceMap = null; // cache indices
	@Override
	public synchronized DatasetInstance lookupInstance(String source) {
		if (this.instanceMap==null){
			logger.info("regenerating instance lookup cache for dataset "+getInfo().getSource());
			this.instanceMap = Maps.newHashMap();
			for (DatasetInstance inst: this){
				instanceMap.put(inst.getInfo().getSource(), inst);
			}
		}
		return this.instanceMap.get(source);
	}

	
}
