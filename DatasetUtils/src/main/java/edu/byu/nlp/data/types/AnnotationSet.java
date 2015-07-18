package edu.byu.nlp.data.types;

import java.util.Map;

import org.apache.commons.math3.linear.SparseRealMatrix;
import org.apache.commons.math3.linear.SparseRealVector;

/**
 * Summarized annotations for a given data item.
 * 
 * @author plf1
 *
 */
public interface AnnotationSet {

	/**
	 * Get a vector containing the number of times that each annotator 
	 * annotated this instance as each label. Indexed by 
	 * [annotator, label]. 
	 */
	SparseRealMatrix getLabelAnnotations();

	/**
	 * Get all of the measurements submitted by this annotator. 
	 * Indexed by [annotator, measurement].
	 */
  Iterable<Measurement<Integer,Integer>> getMeasurements();	
	
	/**
	 * Get the set of raw annotations from which this set was 
	 * generated. 
	 */
	Iterable<Map<String,Object>> getRawAnnotations();
	
	/**
	 * Gets the mean annotation value for each annotator's
	 * annotated regressand. Indexed by [annotator]. 
	 */
	SparseRealVector getRegressandAnnotationMeans();

	/**
	 * Gets the variance of the annotation value for each annotator's
	 * annotated regressand. Indexed by [annotator].
	 */
	SparseRealVector getRegressandAnnotationVariances();

}
