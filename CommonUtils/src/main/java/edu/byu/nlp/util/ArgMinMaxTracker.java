package edu.byu.nlp.util;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class ArgMinMaxTracker<N extends Comparable<N>, T> {

	// the first generic is the value (primary sorting key)
	// second generic is the object contained in a randomized comparable 
	// (so that ties are broken randomly)
  private SortedSet<ComparablePair<N,RandomizedComparableContainer<T>>> argmax = new TreeSet<>();
  private SortedSet<ComparablePair<N,RandomizedComparableContainer<T>>> argmin = new TreeSet<>();
  private int topn;
  private RandomGenerator rnd;

  public ArgMinMaxTracker(RandomGenerator rnd){
    this(rnd,1);
  }
	public ArgMinMaxTracker(RandomGenerator rnd, int topn){
	  this.topn=topn;
	  this.rnd=rnd;
	}

  public static <N extends Comparable<N>,T> ArgMinMaxTracker<N,T> create(RandomGenerator rnd, int topn){
    return new ArgMinMaxTracker<N,T>(rnd,topn);
  }
  
	public static <N extends Comparable<N>,T> ArgMinMaxTracker<N,T> create(RandomGenerator rnd){
		return new ArgMinMaxTracker<N,T>(rnd);
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
		ComparablePair<N, RandomizedComparableContainer<T>> pair = ComparablePair.of(val, RandomizedComparableContainer.of(item, rnd));
    argmax.add(pair);
    argmin.add(pair);

    // peel off all entries not in the top n
    while (argmax.size()>topn){
      argmax.remove(argmax.first());
    }

    // peel off all entries not in the bottom n
    while (argmin.size()>topn){
      argmin.remove(argmin.last());
    }
	}

  /**
   * ordered from high to low
   */
	public List<N> max(){
    List<N> retval = Lists.newArrayList();
    for (ComparablePair<N, RandomizedComparableContainer<T>> pair: argmax){
      retval.add(0,pair.getFirst());
    }
    return retval;
	}
	
	/**
	 * ordered from high to low
	 */
	public List<T> argmax(){
	  List<T> retval = Lists.newArrayList();
	  for (ComparablePair<N, RandomizedComparableContainer<T>> pair: argmax){
	    retval.add(0,pair.getSecond().getValue());
	  }
    return retval;
	}

  /**
   * ordered from low to high
   */
	public List<N> min(){
    List<N> retval = Lists.newArrayList();
    for (ComparablePair<N, RandomizedComparableContainer<T>> pair: argmin){
      retval.add(pair.getFirst());
    }
    return retval;
	}

  /**
   * ordered from low to high
   */
	public List<T> argmin(){
    List<T> retval = Lists.newArrayList();
    for (ComparablePair<N, RandomizedComparableContainer<T>> pair: argmin){
      retval.add(pair.getSecond().getValue());
    }
    return retval;
	}
	
	@Override
	public String toString() {
	  return MoreObjects.toStringHelper(ArgMinMaxTracker.class)
	      .add("argmax", argmax)
	      .add("argmin", argmin)
	      .toString();
	}
	
	
	private static class RandomizedComparableContainer<T> extends Pair<Double,T> implements Comparable<RandomizedComparableContainer<T>>{
	  private static final long serialVersionUID = 1L;
    private RandomizedComparableContainer(Double first, T second) {
      super(first, second);
    }
    public static <T> RandomizedComparableContainer<T> of(T object, RandomGenerator rnd){
      return new RandomizedComparableContainer<T>(rnd.nextDouble(), object);
    }
    @Override
    public int compareTo(RandomizedComparableContainer<T> o) {
      return Double.compare(getFirst(), o.getFirst());
    }
    public T getValue(){
      return getSecond();
    }
	}
	

  /**
   * Exists soley to remove the extra generic from ArgMinMaxTracker
   */
  public static class MinMaxTracker<N extends Comparable<N>> extends ArgMinMaxTracker<N, Object>{
    public MinMaxTracker() {
      // don't worry about introducing uncontrolled randomness here, 
      // since this generator will be ignored
      this(new MersenneTwister());
    }
    public MinMaxTracker(RandomGenerator rnd) {
      super(rnd);
    }
    public static <N extends Comparable<N>> MinMaxTracker<N> create(){
      return new MinMaxTracker<N>(); 
    }
  }
	
}
