package edu.byu.nlp.dataset;

import edu.byu.nlp.data.types.DatasetInstance;
import edu.byu.nlp.data.types.DatasetInstanceInfo;
import edu.byu.nlp.math.SparseRealMatrices;
import edu.byu.nlp.util.Doubles;

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
	
	
}
