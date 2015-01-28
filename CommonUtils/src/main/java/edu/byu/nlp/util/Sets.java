package edu.byu.nlp.util;

import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public class Sets {

	public static double max(Set<Double> set){
		MaxTracker mt = new MaxTracker();
		mt.offerDoubles(set);
		return mt.maxDouble();
	}

	public static double min(Set<Double> set){
		MaxTracker mt = new MaxTracker();
		mt.offerDoubles(Iterables.transform(set,new Function<Double,Double>(){
			@Override
			public Double apply(Double input) {
				return -1*input;
			}
		}));
		return -1*mt.maxDouble();
	}
	
	
}
