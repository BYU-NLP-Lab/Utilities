package edu.byu.nlp.dataset;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.math3.random.RandomGenerator;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.byu.nlp.data.types.Dataset;
import edu.byu.nlp.data.types.DatasetInfo;
import edu.byu.nlp.data.types.DatasetInstance;
import edu.byu.nlp.util.Collections3;
import edu.byu.nlp.util.Indexer;

public class BasicDataset implements Dataset {
	private static final Logger logger = Logger.getLogger(BasicDataset.class.getName());

	private List<DatasetInstance> instances;
	private DatasetInfo info;

	public BasicDataset(Dataset other){
		this.instances = Lists.newArrayList(other);
		this.info = other.getInfo();
	}

	public BasicDataset(String source, Iterable<DatasetInstance> instances, int numDocuments, int numLabeledDocuments, 
		int numTokens, int numLabeledTokens, Indexer<Long> annotatorIdIndexer, Indexer<String> featureIndexer, Indexer<String> labelIndexer, Indexer<Long> instanceIdIndexer){
		this(instances, new Info(source, numDocuments, numLabeledDocuments, numTokens, numLabeledTokens, annotatorIdIndexer, featureIndexer, labelIndexer, instanceIdIndexer));
	}
	
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
		private int numLabeledDocuments;
		private int numTokens;
		private int numLabeledTokens;
		private int numFeatures;
		private int numClasses;
		private Indexer<String> featureIndexer;
		private Indexer<String> labelIndexer;
		private Indexer<Long> annotatorIdIndex;
		private Indexer<Long> instanceIdIndexer;

		public Info(String source, int numDocuments, int numLabeledDocuments, int numTokens, int numLabeledTokens, 
				Indexer<Long> annotatorIdIndex, Indexer<String> featureIndexer, Indexer<String> labelIndexer, Indexer<Long> instanceIdIndexer){
			this.source=source;
			this.numDocuments=numDocuments;
			this.numLabeledDocuments=numLabeledDocuments;
			this.numTokens=numTokens;
			this.numLabeledTokens=numLabeledTokens;
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
		public int getNumLabeledDocuments() {
			return numLabeledDocuments;
		}

		@Override
		public int getNumUnlabeledDocuments() {
			return numDocuments-numLabeledDocuments;
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
		public int getNumLabeledTokens() {
			return numLabeledTokens;
		}

		@Override
		public int getNumUnlabeledTokens() {
			return numTokens - numLabeledTokens;
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