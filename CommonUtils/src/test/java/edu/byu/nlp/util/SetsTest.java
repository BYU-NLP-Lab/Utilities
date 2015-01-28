package edu.byu.nlp.util;

import java.util.Set;

import org.fest.assertions.Assertions;
import org.junit.Test;

public class SetsTest {

	@Test
	public void test() {
		Set<Double> nums = com.google.common.collect.Sets.newHashSet(2.,5.,-3.4,0.);
		Assertions.assertThat(Sets.max(nums)).isEqualTo(5.);
		Assertions.assertThat(Sets.min(nums)).isEqualTo(-3.4);
	}

	@Test
	public void testCorners() {
		Set<Double> nums = com.google.common.collect.Sets.newHashSet(Double.MAX_VALUE,Double.POSITIVE_INFINITY,5.,-3.4,0.);
		Assertions.assertThat(Sets.max(nums)).isEqualTo(Double.POSITIVE_INFINITY);
	}
	
	@Test
	public void testCorners2() {
		Set<Double> nums = com.google.common.collect.Sets.newHashSet(Double.MAX_VALUE,2.,5.,-3.4,0.);
		Assertions.assertThat(Sets.max(nums)).isEqualTo(Double.MAX_VALUE);
		Assertions.assertThat(Sets.min(nums)).isEqualTo(-3.4);
	}
}
