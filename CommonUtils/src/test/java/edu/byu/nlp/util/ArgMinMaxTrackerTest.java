package edu.byu.nlp.util;

import org.fest.assertions.Assertions;
import org.junit.Test;

import edu.byu.nlp.util.ArgMinMaxTracker.MinMaxTracker;

public class ArgMinMaxTrackerTest {

	@Test
	public void testMinMaxDouble(){
		MinMaxTracker<Double> maxer = new MinMaxTracker<Double>();
		maxer.offer(-1.123);
		maxer.offer(5.53);
		Assertions.assertThat(maxer.min()).isEqualTo(-1.123);
		maxer.offer(Double.NEGATIVE_INFINITY);
		maxer.offer(3.234);

		Assertions.assertThat(maxer.max()).isEqualTo(5.53);
		Assertions.assertThat(maxer.min()).isEqualTo(Double.NEGATIVE_INFINITY);
	}

	@Test
	public void testMinMaxInteger(){
		MinMaxTracker<Integer> maxer = new MinMaxTracker<Integer>();
		maxer.offer(-1);
		maxer.offer(5);
		Assertions.assertThat(maxer.min()).isEqualTo(-1);
		maxer.offer(Integer.MIN_VALUE);
		maxer.offer(3);

		Assertions.assertThat(maxer.max()).isEqualTo(5);
		Assertions.assertThat(maxer.min()).isEqualTo(Integer.MIN_VALUE);
	}


	@Test
	public void testArgMinMaxDouble(){
		ArgMinMaxTracker<Double,String> maxer = new ArgMinMaxTracker<Double,String>();
		maxer.offer(-1.123,"-1.123");
		maxer.offer(5.53,"5.53");
		Assertions.assertThat(maxer.min()).isEqualTo(-1.123);
		Assertions.assertThat(maxer.argmin()).contains("-1.123");
		maxer.offer(Double.NEGATIVE_INFINITY);
		maxer.offer(Double.NEGATIVE_INFINITY,"a");
		maxer.offer(Double.NEGATIVE_INFINITY,"b");
		maxer.offer(3.234,"3.234");

		Assertions.assertThat(maxer.max()).isEqualTo(5.53);
		Assertions.assertThat(maxer.argmax()).contains("5.53");
		Assertions.assertThat(maxer.min()).isEqualTo(Double.NEGATIVE_INFINITY);
		Assertions.assertThat(maxer.argmin()).contains(null,"a","b");
	}
	

	@Test
	public void testArgMinMaxInteger(){
		ArgMinMaxTracker<Integer,String> maxer = new ArgMinMaxTracker<Integer,String>();
		maxer.offer(-1,"-1");
		maxer.offer(5,"5");
		Assertions.assertThat(maxer.min()).isEqualTo(-1);
		Assertions.assertThat(maxer.argmin()).contains("-1");
		maxer.offer(Integer.MIN_VALUE);
		maxer.offer(Integer.MIN_VALUE,"X");
		maxer.offer(Integer.MIN_VALUE,"Y");
		maxer.offer(3,"3");

		Assertions.assertThat(maxer.max()).isEqualTo(5);
		Assertions.assertThat(maxer.argmax()).contains("5");
		Assertions.assertThat(maxer.min()).isEqualTo(Integer.MIN_VALUE);
		Assertions.assertThat(maxer.argmin()).contains(null,"X","Y");
	}
	
}
