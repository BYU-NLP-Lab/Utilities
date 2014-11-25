package edu.byu.nlp.dataset;

import edu.byu.nlp.data.types.DatasetInstance;
import edu.byu.nlp.data.types.DatasetInstanceInfo;
import edu.byu.nlp.math.SparseRealMatrices;
import edu.byu.nlp.util.Doubles;

public class DatasetInstances {

	public static boolean isLabelConcealed(DatasetInstance inst) {
		return !inst.hasLabel() && inst.hasConcealedLabel(); 
	}

	public static boolean isRegressandConcealed(DatasetInstance inst) {
		return !inst.hasRegressand() && inst.hasConcealedRegressand(); 
	}

	public static DatasetInstance instanceWithConcealedTruth(DatasetInstance inst){
		return new BasicDatasetInstance(inst.asFeatureVector(), 
				inst.getConcealedLabel(), true, 
				inst.getConcealedRegressand(), true, 
				inst.getAnnotations(), inst.getInfo());
	}

	public static DatasetInstance instanceWithObservedTruth(DatasetInstance inst){
		return new BasicDatasetInstance(inst.asFeatureVector(), 
				inst.getConcealedLabel(), false, 
				inst.getConcealedRegressand(), false, 
				inst.getAnnotations(), inst.getInfo());
	}
	
	
}
