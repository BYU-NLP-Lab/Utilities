package edu.byu.nlp.dataset;

import java.util.logging.Logger;

import edu.byu.nlp.data.types.AnnotationSet;
import edu.byu.nlp.data.types.DatasetInstance;
import edu.byu.nlp.data.types.DatasetInstanceInfo;
import edu.byu.nlp.data.types.SparseFeatureVector;
import edu.byu.nlp.math.SparseRealMatrices;
import edu.byu.nlp.math.SparseRealVectors;
import edu.byu.nlp.util.Indexer;
import edu.byu.nlp.util.Integers;

public class BasicDatasetInstance implements DatasetInstance {

	private static final Logger logger = Logger.getLogger(BasicDatasetInstance.class.getName());
	  
	private SparseFeatureVector featureVector;

	private Integer label;
	private boolean isLabelConcealed = false;

	private Double regressand;
	private boolean isRegressandConcealed = false;

	private AnnotationSet annotations;

	private DatasetInstanceInfo info;

	
	public BasicDatasetInstance(SparseFeatureVector vector,  
			Integer label, boolean isLabelConcealed, Double regressand, boolean isRegressandConcealed, 
			AnnotationSet annotations, long instanceId, String source, Indexer<String> labelIndexer){
		this(vector,label,isLabelConcealed,regressand,isRegressandConcealed,annotations,
				new InstanceInfo(
						instanceId, source, annotations, 
						Integers.fromDouble(SparseRealMatrices.sum(annotations.getLabelAnnotations()), 1e-10),
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
		return hasConcealedLabel() && !isLabelConcealed;
	}

	@Override
	public Integer getLabel() {
		return label; 
	}

	@Override
	public Integer getConcealedLabel() {
		return label;
	}

	@Override
	public boolean hasRegressand() {
		return hasConcealedLabel() && !isRegressandConcealed;
	}

	@Override
	public Double getRegressand() {
		return (hasRegressand())? regressand: null;
	}

	@Override
	public Double getConcealedRegressand() {
		return regressand;
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
		private String source;
		private int numAnnotations;
		private long instanceId;
		private Indexer<String> labelIndexer;
		private AnnotationSet annotations;

		public InstanceInfo(long instanceId, String source, AnnotationSet annotations, int numAnnotations, Indexer<String> labelIndexer){
			this.numAnnotations=numAnnotations;
			this.annotations=annotations;
			this.source=source;
			this.instanceId=instanceId;
			this.labelIndexer=labelIndexer;
		}
		@Override
		public String getSource() {
			return source;
		}
		@Override
		public int getNumAnnotators() {
			return annotations.getLabelAnnotations().getRowDimension();
		}
		@Override
		public int getNumAnnotations() {
			return numAnnotations;
		}
		@Override
		public String toString() {
			return "id="+getInstanceId()+" src="+source+" numannotators="+getNumAnnotators()+" numannotations="+numAnnotations;
		}
		@Override
		public long getInstanceId() {
			return instanceId;
		}
		@Override
		public Indexer<String> getLabelIndexer() {
			return labelIndexer;
		}
		@Override
		public void updateAnnotationInfo() {
			this.numAnnotations = Integers.fromDouble(SparseRealMatrices.sum(annotations.getLabelAnnotations()), 1e-10);
		}
		
	}

	@Override
	public boolean hasAnnotations() {
		return SparseRealMatrices.sum(getAnnotations().getLabelAnnotations())>0 ||
				SparseRealVectors.sum(getAnnotations().getRegressandAnnotationMeans())>0;
	}

	@Override
	public boolean hasConcealedLabel() {
		return label!=null && !label.equals(getInfo().getLabelIndexer().indexOf(null));
	}

	@Override
	public boolean hasConcealedRegressand() {
		return regressand!=null;
	}

}
