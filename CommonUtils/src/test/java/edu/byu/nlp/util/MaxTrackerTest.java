package edu.byu.nlp.util;

import org.fest.assertions.Assertions;
import org.junit.Test;

public class MaxTrackerTest {

	@Test
	public void testMaxDouble(){
		MaxTracker maxer = new MaxTracker();
		maxer.offerDouble(-1.123);
		maxer.offerDouble(5.53);
		maxer.offerDouble(Double.NEGATIVE_INFINITY);
		maxer.offerDouble(3.234);
		
		Assertions.assertThat(maxer.maxDouble()).isEqualTo(5.53);
		Assertions.assertThat(maxer.maxLong()).isEqualTo(Long.MIN_VALUE);
	}

	@Test
	public void testMaxLong(){
		MaxTracker maxer = new MaxTracker();
		maxer.offerLong(-1);
		maxer.offerLong(5);
		maxer.offerLong(Long.MIN_VALUE);
		maxer.offerLong(3);

		Assertions.assertThat(maxer.maxLong()).isEqualTo(5);
		Assertions.assertThat(maxer.maxDouble()).isEqualTo(Double.NEGATIVE_INFINITY);
	}

	@Test
	public void testMax(){
		MaxTracker maxer = new MaxTracker();
		maxer.offerLong(-1);
		maxer.offerLong(5);
		maxer.offerLong(Long.MIN_VALUE);
		maxer.offerDouble(23.42);
		maxer.offerLong(3);
		
		Assertions.assertThat(maxer.maxDouble()).isEqualTo(23.42);
		
		maxer.offerLong(98);
		Assertions.assertThat(maxer.max()).isEqualTo(98);
	}
}
