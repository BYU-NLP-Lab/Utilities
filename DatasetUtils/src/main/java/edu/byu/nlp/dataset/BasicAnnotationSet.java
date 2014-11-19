package edu.byu.nlp.dataset;

import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.SparseRealMatrix;
import org.apache.commons.math3.linear.SparseRealVector;

import edu.byu.nlp.data.types.AnnotationSet;
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

	public BasicAnnotationSet(int numAnnotators, int numClasses){
		this(new OpenMapRealMatrix(numAnnotators, numClasses), null, null);
	}
	public BasicAnnotationSet(SparseRealMatrix labelAnnotations, 
			SparseRealVector regressandMeans, SparseRealVector regressandVariances){
		this.labelAnnotations=labelAnnotations;
		this.regressandMeans=regressandMeans;
		this.regressandVariances=regressandVariances;
	}
	
	/**
	 * Summarize the annotations contained in a table:  
	 * table[instanceId][annotatorId][class] = count
	 * for a single row instanceId
	 */
	public static AnnotationSet fromCountTable(Long instanceIndex, int numAnnotators, int numClasses, TableCounter<Long, Long, Integer> table){
		if (numAnnotators==0 || numClasses==0){
			return null;
		}
		final BasicAnnotationSet annotationSet = new BasicAnnotationSet(numAnnotators, numClasses);
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
	public String toString() {
		return ""+labelAnnotations;
	}

}
