package edu.byu.nlp.dataset;

import org.apache.commons.math3.random.RandomGenerator;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import edu.byu.nlp.data.types.DatasetInstance;
import edu.byu.nlp.math.AbstractRealMatrixPreservingVisitor;
import edu.byu.nlp.util.Integers;
import edu.byu.nlp.util.Multisets2;

public class DatasetInstances {

	public static boolean isLabelConcealed(DatasetInstance inst) {
		return !inst.hasObservedLabel() && inst.hasLabel(); 
	}

	public static boolean isRegressandConcealed(DatasetInstance inst) {
		return !inst.hasObservedRegressand() && inst.hasRegressand(); 
	}

	public static DatasetInstance instanceWithConcealedTruth(DatasetInstance inst){
		return new BasicDatasetInstance(inst.asFeatureVector(), 
				inst.getLabel(), true, 
				inst.getRegressand(), true, 
				inst.getAnnotations(), inst.getInfo());
	}

	public static DatasetInstance instanceWithObservedTruth(DatasetInstance inst){
		return new BasicDatasetInstance(inst.asFeatureVector(), 
				inst.getLabel(), false, 
				inst.getRegressand(), false, 
				inst.getAnnotations(), inst.getInfo());
	}
	
	/**
	 * Returns null if there are no annotations on this instance
	 */
	public static Integer majorityVoteLabel(DatasetInstance inst, RandomGenerator rnd){
		Preconditions.checkNotNull(inst);
		final Multiset<Integer> labelDist = HashMultiset.create();
		inst.getAnnotations().getLabelAnnotations().walkInOptimizedOrder(new AbstractRealMatrixPreservingVisitor() {
			@Override
			public void visit(int annotator, int annotationValue, double value) {
				for (int i=0; i<Math.round(value); i++){
					labelDist.add(annotationValue);
				}
			}
		});
		return Multisets2.maxElement(labelDist, rnd);
	}
	

	public static int numTokensIn(DatasetInstance inst){
//		return Integers.fromDouble(inst.asFeatureVector().sum(), Datasets.INT_CAST_THRESHOLD);
		// allow fractional features
		return Integers.fromDouble(inst.asFeatureVector().sum(), 1);
	}
	
}
