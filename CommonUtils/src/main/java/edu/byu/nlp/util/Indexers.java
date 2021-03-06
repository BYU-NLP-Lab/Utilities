package edu.byu.nlp.util;

import java.util.BitSet;

import org.fest.util.Objects;

public class Indexers {

	/**
	 * Determine whether these indexers agree on what index each 
	 * object should receive. These indexers need not contain all 
	 * of the same items, but one must be a strict subset of the 
	 * other.
	 */
	public static <T> boolean agree(Indexer<T> i1, Indexer<T> i2){
		int minsize = Math.min(i1.size(), i2.size());
		for (int i=0; i<minsize; i++){
			T o1 = i1.get(i);
			T o2 = i2.get(i);
			// ensure that objects for the same index are equal 
			if (!Objects.areEqual(o1, o2)){
				return false;
			}
		}
		return true;
	}

  public static Indexer<String> indexerOfStrings(int numLongs){
    Indexer<String> indexer = new Indexer<String>();
    for (long i=0; i<numLongs; i++){
      indexer.add(""+i);
    }
    return indexer;
  }
  
	public static Indexer<Long> indexerOfLongs(int numLongs){
		Indexer<Long> indexer = new Indexer<Long>();
		for (long i=0; i<numLongs; i++){
			indexer.add(i);
		}
		return indexer;
	}
	
	@SafeVarargs
	public static <T> Indexer<T> indexerOf(T... items){
		Indexer<T> indexer = new Indexer<T>();
		for (T item: items){
			indexer.add(item);
		}
		return indexer;
	}

  /**
   * Index labels eliminate the 'null' label, generated by documents with no
   * label
   */
  public static Indexer<String> removeNullLabel(Indexer<String> labelIndex) {
  	BitSet validLabels = new BitSet();
  	for (int l = 0; l < labelIndex.size(); l++) {
  		String label = labelIndex.get(l);
  		validLabels.set(l, label != null);
  	}
  	labelIndex = labelIndex.retain(validLabels);
  	return labelIndex;
  }
	
}
