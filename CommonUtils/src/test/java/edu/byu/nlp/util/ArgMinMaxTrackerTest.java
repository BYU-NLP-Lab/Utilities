package edu.byu.nlp.util;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.fest.assertions.Assertions;
import org.junit.Test;

import com.google.common.collect.Sets;

import edu.byu.nlp.util.ArgMinMaxTracker.MinMaxTracker;

public class ArgMinMaxTrackerTest {

  private static RandomGenerator rnd = new MersenneTwister();
  
	@Test
	public void testMinMaxDouble(){
		MinMaxTracker<Double> maxer = new MinMaxTracker<Double>(rnd);
		maxer.offer(-1.123);
		maxer.offer(5.53);
    Assertions.assertThat(maxer.min().get(0)).isEqualTo(-1.123);
		maxer.offer(Double.NEGATIVE_INFINITY);
		maxer.offer(3.234);
    Assertions.assertThat(maxer.min().size()).isEqualTo(1);
    Assertions.assertThat(maxer.max().size()).isEqualTo(1);

		Assertions.assertThat(maxer.max().get(0)).isEqualTo(5.53);
		Assertions.assertThat(maxer.min().get(0)).isEqualTo(Double.NEGATIVE_INFINITY);
    Assertions.assertThat(maxer.min().size()).isEqualTo(1);
	}

	@Test
	public void testMinMaxInteger(){
		MinMaxTracker<Integer> maxer = new MinMaxTracker<Integer>(rnd);
		maxer.offer(-1);
		maxer.offer(5);
		Assertions.assertThat(maxer.min().get(0)).isEqualTo(-1);
		maxer.offer(Integer.MIN_VALUE);
		maxer.offer(3);
    Assertions.assertThat(maxer.min().size()).isEqualTo(1);
    Assertions.assertThat(maxer.max().size()).isEqualTo(1);

		Assertions.assertThat(maxer.max().get(0)).isEqualTo(5);
		Assertions.assertThat(maxer.min().get(0)).isEqualTo(Integer.MIN_VALUE);
	}


	@Test
	public void testArgMinMaxDouble(){
		ArgMinMaxTracker<Double,String> maxer = new ArgMinMaxTracker<Double,String>(rnd);
		maxer.offer(-1.123,"-1.123");
		maxer.offer(5.53,"5.53");
		Assertions.assertThat(maxer.min().get(0)).isEqualTo(-1.123);
		Assertions.assertThat(maxer.argmin()).contains("-1.123");
		maxer.offer(Double.NEGATIVE_INFINITY);
		maxer.offer(Double.NEGATIVE_INFINITY,"a");
		maxer.offer(Double.NEGATIVE_INFINITY,"b");
		maxer.offer(3.234,"3.234");

		Assertions.assertThat(maxer.max().get(0)).isEqualTo(5.53);
		Assertions.assertThat(maxer.argmax()).contains("5.53");
		Assertions.assertThat(maxer.min().get(0)).isEqualTo(Double.NEGATIVE_INFINITY);
    Assertions.assertThat(Sets.newHashSet(null,"a","b")).contains(maxer.argmin().get(0));
    Assertions.assertThat(maxer.min().size()).isEqualTo(1);
    Assertions.assertThat(maxer.max().size()).isEqualTo(1);
    Assertions.assertThat(maxer.argmax().size()==1);
    Assertions.assertThat(maxer.argmin().size()==1);
	}
	

  @Test
  public void testArgMinMaxInteger(){
    ArgMinMaxTracker<Integer,String> maxer = new ArgMinMaxTracker<Integer,String>(rnd);
    maxer.offer(-1,"-1");
    maxer.offer(5,"5");
    Assertions.assertThat(maxer.min().get(0)).isEqualTo(-1);
    Assertions.assertThat(maxer.argmin()).contains("-1");
    maxer.offer(Integer.MIN_VALUE);
    maxer.offer(Integer.MIN_VALUE,"X");
    maxer.offer(Integer.MIN_VALUE,"Y");
    maxer.offer(3,"3");

    Assertions.assertThat(maxer.max().get(0)).isEqualTo(5);
    Assertions.assertThat(maxer.argmax()).contains("5");
    Assertions.assertThat(maxer.min().get(0)).isEqualTo(Integer.MIN_VALUE);

    Assertions.assertThat(Sets.newHashSet(null,"X","Y")).contains(maxer.argmin().get(0));
    Assertions.assertThat(maxer.argmax().size()==1);
    Assertions.assertThat(maxer.argmin().size()==1);
    Assertions.assertThat(maxer.min().size()).isEqualTo(1);
    Assertions.assertThat(maxer.max().size()).isEqualTo(1);
  }
  


  @Test
  public void testArgMinMaxDoubleTopn(){
    ArgMinMaxTracker<Double,String> maxer = new ArgMinMaxTracker<Double,String>(rnd,3);
    maxer.offer(-1.123,"-1.123");
    maxer.offer(5.53,"5.53");
    Assertions.assertThat(maxer.min().get(0)).isEqualTo(-1.123);
    Assertions.assertThat(maxer.argmin().get(0)).contains("-1.123");
    Assertions.assertThat(maxer.min().get(1)).isEqualTo(5.53);
    Assertions.assertThat(maxer.argmin().get(1)).contains("5.53");
    maxer.offer(Double.NEGATIVE_INFINITY);
    maxer.offer(Double.NEGATIVE_INFINITY,"a");
    maxer.offer(Double.NEGATIVE_INFINITY,"b");
    maxer.offer(3.234,"3.234");

    Assertions.assertThat(maxer.max().get(0)).isEqualTo(5.53);
    Assertions.assertThat(maxer.argmax().get(0)).contains("5.53");
    Assertions.assertThat(maxer.max().get(1)).isEqualTo(3.234);
    Assertions.assertThat(maxer.argmax().get(1)).contains("3.234");
    Assertions.assertThat(maxer.max().get(2)).isEqualTo(-1.123);
    Assertions.assertThat(maxer.argmax().get(2)).contains("-1.123");
    Assertions.assertThat(maxer.min().get(0)).isEqualTo(Double.NEGATIVE_INFINITY);
    Assertions.assertThat(maxer.argmin()).contains(null,"a","b");
    Assertions.assertThat(maxer.argmax().size()==3);
    Assertions.assertThat(maxer.argmin().size()==3);
    Assertions.assertThat(maxer.max().size()==3);
    Assertions.assertThat(maxer.min().size()==3);
  }
  

  @Test
  public void testArgMinMaxIntegerTopn(){
    ArgMinMaxTracker<Integer,String> maxer = new ArgMinMaxTracker<Integer,String>(rnd,3);
    maxer.offer(-1,"-1");
    maxer.offer(5,"5");
    Assertions.assertThat(maxer.min().get(0)).isEqualTo(-1);
    Assertions.assertThat(maxer.argmin()).contains("-1");
    maxer.offer(Integer.MIN_VALUE);
    maxer.offer(Integer.MIN_VALUE,"X");
    maxer.offer(Integer.MIN_VALUE,"Y");
    maxer.offer(3,"3");

    Assertions.assertThat(maxer.max().get(0)).isEqualTo(5);
    Assertions.assertThat(maxer.argmax().get(0)).isEqualTo("5");
    Assertions.assertThat(maxer.min().get(0)).isEqualTo(Integer.MIN_VALUE);
    Assertions.assertThat(maxer.argmin()).contains(null,"X","Y");
    Assertions.assertThat(maxer.argmax().size()==1);
    Assertions.assertThat(maxer.argmin().size()==3);
  }
	
}
