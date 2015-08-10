package edu.byu.nlp.data.types;

import java.util.Collection;

import org.apache.commons.math3.linear.SparseRealMatrix;
import org.apache.commons.math3.linear.SparseRealVector;

import edu.byu.nlp.data.FlatInstance;

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
	 * Get the set of raw annotations from which this set was 
	 * generated. 
	 */
  Collection<FlatInstance<SparseFeatureVector, Integer>> getRawAnnotations();
	
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
