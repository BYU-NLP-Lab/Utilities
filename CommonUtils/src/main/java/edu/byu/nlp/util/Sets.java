package edu.byu.nlp.util;

import java.util.Set;

import edu.byu.nlp.util.ArgMinMaxTracker.MinMaxTracker;

public class Sets {

	public static double max(Set<Double> set){
		MinMaxTracker<Double> mt = new MinMaxTracker<Double>();
		mt.offer(set);
		return mt.max().get(0);
	}

	public static double min(Set<Double> set){
		MinMaxTracker<Double> mt = new MinMaxTracker<Double>();
		mt.offer(set);
		return mt.min().get(0);
	}
	
	
}
