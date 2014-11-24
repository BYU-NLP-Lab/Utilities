package edu.byu.nlp.math;

import org.apache.commons.math3.linear.SparseRealVector;

public class SparseRealVectors {
	
	public static double sum(SparseRealVector vec){
		if (vec==null){
			return 0;
		}
		if (vec.getDimension()==0){
			return 0;
		}
		return RealVectors.sum(vec);
	}

}
