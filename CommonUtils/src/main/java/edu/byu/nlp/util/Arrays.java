package edu.byu.nlp.util;

import java.util.Iterator;

public class Arrays {

	/**
	 * Iterator that lazily traverses the indicated 
	 * portion of the given array.
	 * @param arr
	 * @param startIndex 
	 * @param length
	 */
	public static <T> Iterator<T> subsequence(final T[] arr, final int startIndex, int length){
		final int endIndex = Math.min(arr.length,startIndex+length); // exclusive
		
		return new Iterator<T>(){
			private int index = startIndex;
			@Override
			public boolean hasNext() {
				return index<endIndex;
			}
			@Override
			public T next() {
				return arr[index++];
			}
		};
		
	}

	public static <T> Iterator<T> subsequence(final T[] arr, final int startIndex){
		return subsequence(arr,startIndex,arr.length);
	}
	
}
