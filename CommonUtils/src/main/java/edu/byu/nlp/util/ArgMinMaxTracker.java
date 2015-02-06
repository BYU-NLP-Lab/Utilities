package edu.byu.nlp.util;

import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

public class ArgMinMaxTracker<N extends Comparable<N>, T> {

	private N max = null;
	private Set<T> argmax = Sets.newHashSet();

	private N min = null;
	private Set<T> argmin = Sets.newHashSet();

	public static <N extends Comparable<N>,T> ArgMinMaxTracker<N,T> newArgMinMaxTracker(){
		return new ArgMinMaxTracker<N,T>();
	}

	public void offer(Iterable<N> vals, Iterable<T> items){
		for (Pair<N, T> pair: Iterables2.pairUp(vals, items)){
			offer(pair.first, pair.second);
		}
	}
	
	public void offer(Iterable<N> vals){
		for (N val: vals){
			offer(val);
		}
	}
	
	public void offer(N val){
		offer(val,null);
	}
	
	public void offer(N val, T item){
		Preconditions.checkNotNull(val);
		if (max==null || val.compareTo(max)>0){
			max = val;
			argmax.clear();
		}
		if (val.compareTo(max)>=0){
			argmax.add(item);
		}
		
		if (min==null || val.compareTo(min)<0){
			min = val;
			argmin.clear();
		}
		if (val.compareTo(min)<=0){
			argmin.add(item);
		}
	}
	
	public N max(){
		return max;
	}
	
	public Set<T> argmax(){
		return argmax;
	}
	
	public N min(){
		return min;
	}
	
	public Set<T> argmin(){
		return argmin;
	}
	
	public static class MinMaxTracker<N extends Comparable<N>> extends ArgMinMaxTracker<N, Object>{
		public static <N extends Comparable<N>> MinMaxTracker<N> newMinMaxTracker(){
			return new MinMaxTracker<N>();
		}
	}
	
}
