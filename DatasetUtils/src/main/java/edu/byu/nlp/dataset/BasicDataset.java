package edu.byu.nlp.dataset;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import com.google.common.collect.Lists;

import edu.byu.nlp.data.types.Dataset;
import edu.byu.nlp.data.types.DatasetInfo;
import edu.byu.nlp.data.types.DatasetInstance;
import edu.byu.nlp.util.Collections3;
import edu.byu.nlp.util.Indexer;

public class BasicDataset implements Dataset {

	private List<DatasetInstance> instances;
	private DatasetInfo info;

	public BasicDataset(Dataset other){
		this.instances = Lists.newArrayList(other);
		this.info = other.getInfo();
	}

	public BasicDataset(String source, Collection<DatasetInstance> instances, 
		int numTokens, Indexer<Long> annotatorIdIndexer, Indexer<String> featureIndexer, Indexer<String> labelIndexer, Indexer<Long> instanceIdIndexer){
		this(instances, new Info(source, instances.size(), numTokens, annotatorIdIndexer, featureIndexer, labelIndexer, instanceIdIndexer));
	}
	
	public BasicDataset(Collection<DatasetInstance> instances, DatasetInfo info){
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
		private int numTokens;
		private int numFeatures;
		private int numClasses;
		private Indexer<String> featureIndexer;
		private Indexer<String> labelIndexer;
		private Indexer<Long> annotatorIdIndex;
		private Indexer<Long> instanceIdIndexer;

		public Info(String source, int numDocuments, int numTokens, Indexer<Long> annotatorIdIndex, Indexer<String> featureIndexer, Indexer<String> labelIndexer, Indexer<Long> instanceIdIndexer){
			this.source=source;
			this.numDocuments=numDocuments;
			this.numTokens=numTokens;
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
		public Indexer<Long> getInstanceIdIndexer() {
			return instanceIdIndexer;
		}
		@Override
		public String toString() {
			return "numdocs="+numDocuments+" numtok="+numTokens+" numfeat="+numFeatures+" numclass="+numClasses+" src="+source;
		}
	}
	
}
