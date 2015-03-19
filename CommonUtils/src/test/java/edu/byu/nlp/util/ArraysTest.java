package edu.byu.nlp.util;

import java.util.Iterator;

import org.fest.assertions.Assertions;
import org.junit.Test;

public class ArraysTest {

	private static final String[] A = new String[]{"a","b","c","d"};

	@Test
	public void testSubsequence1(){
		Iterator<String> itr = Arrays.subsequence(A, 0);
		Assertions.assertThat(itr.next()).isEqualTo("a");
		Assertions.assertThat(itr.next()).isEqualTo("b");
		Assertions.assertThat(itr.next()).isEqualTo("c");
		Assertions.assertThat(itr.next()).isEqualTo("d");
		Assertions.assertThat(itr.hasNext()).isFalse();
	}

	@Test
	public void testSubsequence2(){
		Iterator<String> itr = Arrays.subsequence(A, 1);
		Assertions.assertThat(itr.next()).isEqualTo("b");
		Assertions.assertThat(itr.next()).isEqualTo("c");
		Assertions.assertThat(itr.next()).isEqualTo("d");
		Assertions.assertThat(itr.hasNext()).isFalse();
	}

	@Test
	public void testSubsequence3(){
		Iterator<String> itr = Arrays.subsequence(A, 3);
		Assertions.assertThat(itr.next()).isEqualTo("d");
		Assertions.assertThat(itr.hasNext()).isFalse();
	}

	@Test
	public void testSubsequence4(){
		Iterator<String> itr = Arrays.subsequence(A, 1, 2);
		Assertions.assertThat(itr.next()).isEqualTo("b");
		Assertions.assertThat(itr.next()).isEqualTo("c");
		Assertions.assertThat(itr.hasNext()).isFalse();
	}

	@Test
	public void testArray1(){
		String[] a = Arrays.subarray(A, 1, 2);
		Assertions.assertThat(a).isEqualTo(new String[]{"b","c"});
	}

	@Test
	public void testArray2(){
		String[] a = Arrays.subarray(A, 1);
		Assertions.assertThat(a).isEqualTo(new String[]{"b","c","d"});
	}
	
}
