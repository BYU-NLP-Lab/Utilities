package edu.byu.nlp.dataset;

import java.util.Map;

import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.SparseRealMatrix;
import org.apache.commons.math3.linear.SparseRealVector;

import edu.byu.nlp.data.types.AnnotationSet;
import edu.byu.nlp.data.types.Measurement;
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
	private Iterable<Map<String,Object>> rawLabelAnnotations;

	public BasicAnnotationSet(int numAnnotators, int numClasses, Iterable<Map<String,Object>> rawLabelAnnotations){
		this(
				((numAnnotators==0 || numClasses==0)? 
						SparseRealMatrices.nullRowSparseMatrix(numClasses): 
							new OpenMapRealMatrix(numAnnotators, numClasses)), 
						rawLabelAnnotations, null, null);
	}
	public BasicAnnotationSet(
	    SparseRealMatrix labelAnnotations, 
			Iterable<Map<String,Object>> rawLabelAnnotations, 
			SparseRealVector regressandMeans, SparseRealVector regressandVariances){
		this.labelAnnotations=labelAnnotations;
		this.rawLabelAnnotations=rawLabelAnnotations;
		this.regressandMeans=regressandMeans;
		this.regressandVariances=regressandVariances;
	}
	
	/**
	 * Summarize the annotations contained in a table:  
	 * table[instanceId][annotatorId][class] = count
	 * for a single row instanceId
	 */
	public static AnnotationSet fromCountTable(Integer instanceIndex, int numAnnotators, int numClasses, TableCounter<Integer, Integer, Integer> table, 
			Iterable<Map<String,Object>> rawAnnotationValues){
		final BasicAnnotationSet annotationSet = new BasicAnnotationSet(numAnnotators, numClasses, rawAnnotationValues);
		table.visitRowEntriesSparsely(instanceIndex, new SparseTableVisitor<Integer, Integer, Integer>() {
			@Override
			public void visitEntry(Integer row, Integer col, Integer item, int count) {
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
	public Iterable<Map<String,Object>> getRawAnnotations() {
		return rawLabelAnnotations;
	}
	
	@Override
	public String toString() {
		return ""+labelAnnotations;
	}
  @Override
  public Iterable<Measurement<Integer, Integer>> getMeasurements() {
    // TODO Auto-generated method stub
    return null;
  }
	
}
