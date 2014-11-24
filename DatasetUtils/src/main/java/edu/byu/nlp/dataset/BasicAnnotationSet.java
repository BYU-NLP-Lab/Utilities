package edu.byu.nlp.dataset;

import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.SparseRealMatrix;
import org.apache.commons.math3.linear.SparseRealVector;

import edu.byu.nlp.data.FlatInstance;
import edu.byu.nlp.data.types.AnnotationSet;
import edu.byu.nlp.data.types.SparseFeatureVector;
import edu.byu.nlp.math.SparseRealMatrices;
import edu.byu.nlp.util.TableCounter;
import edu.byu.nlp.util.TableCounter.SparseTableVisitor;

/**
 * 
 * @author plf1
 *
 * Contains summarized information about all the annotations associated
 * with a particular dataset instance
 *
 */
public class BasicAnnotationSet implements AnnotationSet{

	private SparseRealMatrix labelAnnotations;
	private SparseRealVector regressandMeans;
	private SparseRealVector regressandVariances;
	private Iterable<FlatInstance<SparseFeatureVector, Integer>> rawLabelAnnotations;
	private Iterable<FlatInstance<SparseFeatureVector, Double>> rawRegressandAnnotations;

	public BasicAnnotationSet(int numAnnotators, int numClasses, Iterable<FlatInstance<SparseFeatureVector, Integer>> rawLabelAnnotations){
		this(
				((numAnnotators==0 || numClasses==0)? 
						SparseRealMatrices.nullRowSparseMatrix(numClasses): 
							new OpenMapRealMatrix(numAnnotators, numClasses)), 
						rawLabelAnnotations, null, null);
	}
	public BasicAnnotationSet(SparseRealMatrix labelAnnotations, 
			Iterable<FlatInstance<SparseFeatureVector, Integer>> rawLabelAnnotations, 
			SparseRealVector regressandMeans, SparseRealVector regressandVariances){
		this(labelAnnotations, rawLabelAnnotations, regressandMeans, regressandVariances, null);
	}
	public BasicAnnotationSet(SparseRealMatrix labelAnnotations, Iterable<FlatInstance<SparseFeatureVector, Integer>> rawLabelAnnotations,
			SparseRealVector regressandMeans, SparseRealVector regressandVariances, Iterable<FlatInstance<SparseFeatureVector, Double>> rawRegressandAnnotations){
		this.labelAnnotations=labelAnnotations;
		this.rawLabelAnnotations=rawLabelAnnotations;
		this.regressandMeans=regressandMeans;
		this.regressandVariances=regressandVariances;
		this.rawRegressandAnnotations=rawRegressandAnnotations;
	}
	
	/**
	 * Summarize the annotations contained in a table:  
	 * table[instanceId][annotatorId][class] = count
	 * for a single row instanceId
	 */
	public static AnnotationSet fromCountTable(Long instanceIndex, int numAnnotators, int numClasses, TableCounter<Long, Long, Integer> table, 
			Iterable<FlatInstance<SparseFeatureVector, Integer>> rawAnnotationValues){
		final BasicAnnotationSet annotationSet = new BasicAnnotationSet(numAnnotators, numClasses, rawAnnotationValues);
		table.visitRowEntriesSparsely(instanceIndex, new SparseTableVisitor<Long, Long, Integer>() {
			@Override
			public void visitEntry(Long row, Long col, Integer item, int count) {
				annotationSet.getLabelAnnotations().setEntry((int)(long)col, item, count);
			}
		});
		return annotationSet;
	}
	
	
	@Override
	public SparseRealVector getRegressandAnnotationMeans() {
		return regressandMeans;
	}

	@Override
	public SparseRealVector getRegressandAnnotationVariances() {
		return regressandVariances;
	}

	@Override
	public SparseRealMatrix getLabelAnnotations() {
		return labelAnnotations;
	}
	
	@Override
	public Iterable<FlatInstance<SparseFeatureVector, Integer>> getRawLabelAnnotations() {
		return rawLabelAnnotations;
	}
	
	@Override
	public Iterable<FlatInstance<SparseFeatureVector, Double>> getRawRegressandAnnotations() {
		return rawRegressandAnnotations;
	}

	@Override
	public String toString() {
		return ""+labelAnnotations;
	}
	
}
