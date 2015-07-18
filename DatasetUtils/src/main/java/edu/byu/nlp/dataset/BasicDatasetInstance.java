package edu.byu.nlp.dataset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.byu.nlp.data.types.AnnotationSet;
import edu.byu.nlp.data.types.DatasetInstance;
import edu.byu.nlp.data.types.DatasetInstanceInfo;
import edu.byu.nlp.data.types.SparseFeatureVector;
import edu.byu.nlp.math.SparseRealMatrices;
import edu.byu.nlp.math.SparseRealVectors;
import edu.byu.nlp.util.Indexer;
import edu.byu.nlp.util.Integers;

public class BasicDatasetInstance implements DatasetInstance {

	private static final Logger logger = LoggerFactory.getLogger(BasicDatasetInstance.class);
	  
	private SparseFeatureVector featureVector;

	private Integer label;
	private boolean isLabelConcealed = false;

	private Double regressand;
	private boolean isRegressandConcealed = false;

	private AnnotationSet annotations;

	private DatasetInstanceInfo info;

	/**
	 * Create a simple instance with an observed label and no annotations or regressand
	 */
	public BasicDatasetInstance(SparseFeatureVector vector, Integer label, int source, Indexer<String> labelIndexer){
		this(vector, label, false, null, false, Datasets.emptyAnnotationSet(), source, labelIndexer);
	}
	
	public BasicDatasetInstance(SparseFeatureVector vector,  
			Integer label, boolean isLabelConcealed, Double regressand, boolean isRegressandConcealed, 
			AnnotationSet annotations, int source, Indexer<String> labelIndexer){
		this(vector,label,isLabelConcealed,regressand,isRegressandConcealed,annotations,
				new InstanceInfo(
						source, annotations, 
						labelIndexer));
	}
	
	
	public BasicDatasetInstance(SparseFeatureVector vector, 
			Integer label, boolean isLabelConcealed, Double regressand, boolean isRegressandConcealed, 
			AnnotationSet annotations, DatasetInstanceInfo info){
		this.featureVector = vector;
		this.label=label;
		this.isLabelConcealed=isLabelConcealed;
		this.regressand=regressand;
		this.isRegressandConcealed=isRegressandConcealed;
		this.annotations=annotations;
		this.info=info;
	}
	
	@Override
	public SparseFeatureVector asFeatureVector() {
		if (featureVector!=null){
			return featureVector;
		}
		else{
			throw new IllegalStateException("Asked an instance defined as a feature maxtrix to be returned as a vector. "
					+ "This feature is not implemented; moreover, it's possible that this is a mistake.");
		}
	}

	@Override
	public DatasetInstanceInfo getInfo() {
		return info;
	}

	@Override
	public boolean hasLabel() {
		return label!=null && !label.equals(getInfo().getLabelIndexer().indexOf(null));
	}

	@Override
	public Integer getLabel() {
		return hasLabel()? label: null;
	}

	@Override
	public boolean hasObservedLabel() {
		return hasLabel() && !isLabelConcealed;
	}

	@Override
	public Integer getObservedLabel() {
		return hasObservedLabel()? label: null; 
	}

	@Override
	public boolean hasObservedRegressand() {
		return hasLabel() && !isRegressandConcealed;
	}

	@Override
	public Double getObservedRegressand() {
		return (hasObservedRegressand())? regressand: null;
	}

	@Override
	public boolean hasRegressand() {
		return regressand!=null;
	}
	
	@Override
	public Double getRegressand() {
		return hasRegressand()? regressand: null;
	}

	@Override
	public boolean hasAnnotations() {
		return SparseRealMatrices.sum(getAnnotations().getLabelAnnotations())>0 ||
				SparseRealVectors.sum(getAnnotations().getRegressandAnnotationMeans())>0;
	}

	@Override
	public AnnotationSet getAnnotations() {
		return annotations;
	}
	
	@Override
	public String toString() {
		return info.toString()+" label="+label+" regressand="+regressand+" annotations="+annotations;
	}
	
	
	public static class InstanceInfo implements DatasetInstanceInfo{
		private int source;
		private int numAnnotations = -1;
		private Indexer<String> labelIndexer;
		private AnnotationSet annotations;

		public InstanceInfo(int source, AnnotationSet annotations, Indexer<String> labelIndexer){
			this.annotations=annotations;
			this.source=source;
			this.labelIndexer=labelIndexer;
		}
		@Override
		public int getSource() {
			return source;
		}
		@Override
		public int getNumAnnotators() {
			return SparseRealMatrices.numNonZeroRows(annotations.getLabelAnnotations());
		}
		@Override
		public int getNumAnnotations() {
			if (numAnnotations==-1){
				numAnnotations = Integers.fromDouble(SparseRealMatrices.sum(annotations.getLabelAnnotations()), Datasets.INT_CAST_THRESHOLD);
			}
			return numAnnotations;
		}
		@Override
		public String toString() {
			return "src="+source+" numannotators="+getNumAnnotators()+" numannotations="+getNumAnnotations();
		}
		@Override
		public Indexer<String> getLabelIndexer() {
			return labelIndexer;
		}
		@Override
		public void annotationsChanged() {
			this.numAnnotations = -1;
		}
		
	}


}
