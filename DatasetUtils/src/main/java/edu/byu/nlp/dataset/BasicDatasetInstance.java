package edu.byu.nlp.dataset;

import java.util.logging.Logger;

import edu.byu.nlp.data.types.AnnotationSet;
import edu.byu.nlp.data.types.DatasetInstance;
import edu.byu.nlp.data.types.DatasetInstanceInfo;
import edu.byu.nlp.data.types.SparseFeatureMatrix;
import edu.byu.nlp.data.types.SparseFeatureVector;
import edu.byu.nlp.math.SparseRealMatrices;
import edu.byu.nlp.math.SparseRealVectors;
import edu.byu.nlp.util.Indexer;

public class BasicDatasetInstance implements DatasetInstance {

	private static final Logger logger = Logger.getLogger(BasicDatasetInstance.class.getName());
	  
	private SparseFeatureMatrix featureMatrix;
	private SparseFeatureVector featureVector;

	private Integer label;
	private boolean isLabelConcealed = false;

	private Double regressand;
	private boolean isRegressandConcealed = false;

	private AnnotationSet annotations;

	private DatasetInstanceInfo info;

	private Indexer<String> labelIndexer;

	public BasicDatasetInstance(SparseFeatureVector vector, 
			Integer label, Double regressand, AnnotationSet annotations, long instanceId, String source, Indexer<String> labelIndexer){
		this(vector, null, label, regressand, annotations, instanceId, source, labelIndexer);
	}
	
	public BasicDatasetInstance(SparseFeatureMatrix matrix, 
			Integer label, Double regressand, AnnotationSet annotations, long instanceId, String source, Indexer<String> labelIndexer){
		this(null, matrix, label, regressand, annotations, instanceId, source, labelIndexer);
	}

	public BasicDatasetInstance(SparseFeatureVector vector, SparseFeatureMatrix matrix, 
			Integer label, Double regressand, AnnotationSet annotations, long instanceId, String source, Indexer<String> labelIndexer){
		this(vector,matrix,label,regressand,annotations,
				new InstanceInfo(
						instanceId, source, annotations.getLabelAnnotations().getRowDimension(), 
						(int) SparseRealMatrices.sum(annotations.getLabelAnnotations())),
				labelIndexer);
	}
	
	
	public BasicDatasetInstance(SparseFeatureVector vector, SparseFeatureMatrix matrix, 
			Integer label, Double regressand, AnnotationSet annotations, DatasetInstanceInfo info, Indexer<String> labelIndexer){
		this.featureVector = vector;
		this.featureMatrix=matrix;
		this.label=label;
		this.regressand=regressand;
		this.annotations=annotations;
		this.info=info;
		this.labelIndexer=labelIndexer;
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
	public SparseFeatureMatrix asFeatureMatrix() {
		if (featureMatrix!=null){
			return featureMatrix;
		}
		else{
			logger.warning("Asked an instance defined as a feature vector to be returned as a feature matrix. "
					+ "Complying, but it's possible that this is a mistake.");
			return new SingleRowFeatureMatrix(featureVector);
		}
	}

	@Override
	public DatasetInstanceInfo getInfo() {
		return info;
	}

	@Override
	public boolean hasLabel() {
		return label!=null && !label.equals(labelIndexer.indexOf(null)) && !isLabelConcealed;
	}

	@Override
	public Integer getLabel() {
		return label; 
	}

	@Override
	public void setLabelConcealed(boolean concealed) {
		this.isLabelConcealed = concealed;
	}

	@Override
	public Integer getConcealedLabel() {
		return label;
	}

	@Override
	public boolean hasRegressand() {
		return regressand!=null && !isRegressandConcealed;
	}

	@Override
	public Double getRegressand() {
		return (hasRegressand())? regressand: null;
	}

	@Override
	public void setRegressandConcealed(boolean concealed) {
		this.isRegressandConcealed = concealed;
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
		private int numAnnotators;
		private long instanceId;

		public InstanceInfo(long instanceId, String source, int numAnnotators, int numAnnotations){
			this.numAnnotations=numAnnotations;
			this.numAnnotators=numAnnotators;
			this.source=source;
			this.instanceId=instanceId;
		}
		
		@Override
		public String getSource() {
			return source;
		}

		@Override
		public int getNumAnnotators() {
			return numAnnotators;
		}

		@Override
		public int getNumAnnotations() {
			return numAnnotations;
		}
		
		@Override
		public String toString() {
			return "src="+source+" numannotators="+numAnnotators+" numannotations="+numAnnotations;
		}

		@Override
		public long getInstanceId() {
			return instanceId;
		}
		
	}


	@Override
	public boolean hasAnnotations() {
		return SparseRealMatrices.sum(getAnnotations().getLabelAnnotations())>0 ||
				SparseRealVectors.sum(getAnnotations().getRegressandAnnotationMeans())>0;
	}

}
