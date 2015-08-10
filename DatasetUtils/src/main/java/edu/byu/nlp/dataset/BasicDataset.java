package edu.byu.nlp.dataset;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.random.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.byu.nlp.data.streams.IndexerCalculator;
import edu.byu.nlp.data.types.Dataset;
import edu.byu.nlp.data.types.DatasetInfo;
import edu.byu.nlp.data.types.DatasetInstance;
import edu.byu.nlp.data.types.Measurement;
import edu.byu.nlp.util.Collections3;
import edu.byu.nlp.util.Indexer;

public class BasicDataset implements Dataset {
	private static final Logger logger = LoggerFactory.getLogger(BasicDataset.class);

	private List<DatasetInstance> instances;
	private DatasetInfo info;
  private Collection<Measurement> measurements;

	public BasicDataset(Dataset other){
		this.instances = Lists.newArrayList(other);
		this.measurements = Sets.newHashSet(other.getMeasurements());
		this.info = other.getInfo();
	}
	
	/**
	 * This is the constructor you should use if you are creating a Dataset from scratch
	 * (rather than transforming an existing dataset). 
	 * Creates a dataset with info calculated from instances.
	 */
	public BasicDataset(String source, Iterable<DatasetInstance> instances, Collection<Measurement> measurements,
	    IndexerCalculator<String, String> indexers){
		this(instances, measurements, Datasets.infoWithCalculatedCounts(instances, source, indexers));
	}

	/**
	 * Creates a dataset with an info from provided stats. This method trusts that you got it right, 
	 * and does not double-check your work.
	 */
	public BasicDataset(String source, Iterable<DatasetInstance> instances, Collection<Measurement> measurements,
			int numDocuments, int numDocumentsWithLabels, int numDocumentsWithObservedLabels, 
		int numTokens, int numTokensWithAnnotations, int numTokensWithLabels, int numTokensWithObservedLabels, int numAnnotations,
    IndexerCalculator<String, String> indexers){
		this(instances, measurements, new Info(source, 
				numDocuments, numDocumentsWithLabels, numDocumentsWithObservedLabels, 
				numTokens, numTokensWithLabels, numTokensWithObservedLabels,  
				indexers, instances));
	}
	
	/**
	 * Creates a dataset with the given info. Trusts that the stats in the info 
	 * are correct, and does not double-check your work.
	 */
	public BasicDataset(Iterable<DatasetInstance> instances, Collection<Measurement> measurements, DatasetInfo info){
	  Preconditions.checkNotNull(instances);
	  Preconditions.checkNotNull(measurements);
		this.instances = Lists.newArrayList(instances);
		this.measurements=Sets.newHashSet(measurements);
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
		private int numDocumentsWithLabels;
		private int numDocumentsWithObservedLabels;
		private int numTokens;
		private int numTokensWithLabels;
		private int numTokensWithObservedLabels;
		private int numFeatures;
		private int numClasses;
		private IndexerCalculator<String, String> indexers;
		private Iterable<DatasetInstance> instances;
		private int numAnnotations = -1;
		private int numDocumentsWithAnnotations = -1;
		private int numTokensWithAnnotations = -1;

		public Info(String source, 
				int numDocuments, int numDocumentsWithLabels, int numDocumentsWithObservedLabels, 
				int numTokens, int numTokensWithLabels, int numTokensWithObservedLabels,
				IndexerCalculator<String, String> indexers, 
				Iterable<DatasetInstance> instances){
			this.source=source;
			this.numDocuments=numDocuments;
			this.numDocumentsWithLabels=numDocumentsWithLabels;
			this.numDocumentsWithObservedLabels=numDocumentsWithObservedLabels;
			this.numTokens=numTokens;
			this.numTokensWithLabels=numTokensWithLabels;
			this.numTokensWithObservedLabels=numTokensWithObservedLabels;
			this.instances=instances;
			this.numFeatures=indexers.getWordIndexer().size();
			this.numClasses=indexers.getLabelIndexer().size();
			this.indexers=indexers;
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
			if (numDocumentsWithAnnotations ==-1){
				numDocumentsWithAnnotations = Datasets.numDocumentsWithAnnotationsIn(instances);
			}
			return numDocumentsWithAnnotations;
		}

		@Override
		public int getNumDocumentsWithoutAnnotations() {
			return numDocuments - getNumDocumentsWithAnnotations();
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
			return indexers.getLabelIndexer();
		}

		@Override
		public Indexer<String> getFeatureIndexer() {
			return indexers.getWordIndexer();
		}

		@Override
		public Indexer<String> getAnnotatorIdIndexer() {
			return indexers.getAnnotatorIdIndexer();
		}
		
    @Override
    public Indexer<String> getInstanceIdIndexer() {
      return indexers.getInstanceIdIndexer();
    }

    @Override
		public IndexerCalculator<String,String> getIndexers(){
		  return indexers;
		}

		@Override
		public int getNumTokens() {
			return numTokens;
		}

		@Override
		public int getNumTokensWithAnnotations() {
			if (numTokensWithAnnotations ==-1){
				numTokensWithAnnotations = Datasets.numTokensWithAnnotationsIn(instances);
			}
			return numTokensWithAnnotations;
		}

		@Override
		public int getNumTokensWithoutAnnotations() {
			return numTokens - getNumTokensWithAnnotations();
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
		public String toString() {
			return "numdocs="+numDocuments+" numtok="+numTokens+" numfeat="+numFeatures+" numclass="+numClasses+" src="+source;
		}

		@Override
		public int getNullLabel() {
			return getLabelIndexer().indexOf(null);
		}

		@Override
		public int getNumAnnotators() {
			return getAnnotatorIdIndexer().size();
		}

		@Override
		public int getNumAnnotations() {
			if (numAnnotations==-1){
				numAnnotations = Datasets.numAnnotationsIn(instances);
			}
			return numAnnotations;
		}

		/**
		 * Annotations are the only mutable aspect of a dataset. They were 
		 * made mutable purely for expediency reasons--it would probably be more ideal  
		 * to refactor so that this is not necessary.
		 */
		@Override
		public void annotationsChanged() {
			this.numAnnotations = -1;
			this.numTokensWithAnnotations = -1;
			this.numDocumentsWithAnnotations = -1;
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
				instanceMap.put(inst.getInfo().getRawSource(), inst);
			}
		}
		return this.instanceMap.get(source);
	}

  @Override
  public Collection<Measurement> getMeasurements() {
    return measurements;
  }

	
}
