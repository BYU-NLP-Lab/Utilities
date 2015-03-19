package edu.byu.nlp.util;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

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
		return subsequence(arr,startIndex,arr.length-startIndex);
	}

	public static <T> List<T> sublist(final T[] arr, final int startIndex){
		return sublist(arr, startIndex, arr.length-startIndex);
	}
	
	public static <T> List<T> sublist(final T[] arr, final int startIndex, int length){
		return Lists.newArrayList(subsequence(arr, startIndex, length));
	}
	
	public static <T> T[] subarray(final T[] arr, final int startIndex){
		return subarray(arr, startIndex, arr.length-startIndex);
	}
	@SuppressWarnings("unchecked")
	public static <T> T[] subarray(final T[] arr, final int startIndex, int length){
		T[] dummyArray = (T[]) Array.newInstance(arr.getClass().getComponentType(), 0);
		return (T[]) sublist(arr, startIndex, length).toArray(dummyArray);
	}
}
