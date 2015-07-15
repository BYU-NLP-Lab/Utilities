package edu.byu.nlp.stats;

import static org.junit.Assert.*;

import org.junit.Test;

public class MutableSumTest {

  private static final double THRESHOLD = 1e-20;
  
  @Test
  public void test() {
    
    MutableSum sum = new MutableSum();
    assertEquals(0,sum.getSum(),THRESHOLD);

    sum.setSummand(0, 1);
    assertEquals(1,sum.getSum(),THRESHOLD);

    sum.setSummand(0, -1);
    assertEquals(-1,sum.getSum(),THRESHOLD);

    sum.setSummand(1, 5.5);
    assertEquals(4.5,sum.getSum(),THRESHOLD);

    sum.setSummand(1, 0);
    assertEquals(-1,sum.getSum(),THRESHOLD);

    sum.setSummand(8, 0);
    assertEquals(-1,sum.getSum(),THRESHOLD);
    
  }

}
