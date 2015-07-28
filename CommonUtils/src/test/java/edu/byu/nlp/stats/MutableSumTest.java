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

  @Test
  public void testActive() {

    MutableSum sum = new MutableSum();
    for (int i=0; i<20; i++){
      sum.setSummand(i, 2);
    }
    assertEquals(40,sum.getSum(),THRESHOLD);

    sum.setSummandActive(0, false);
    assertEquals(38,sum.getSum(),THRESHOLD);

    sum.setSummandActive(0, true);
    assertEquals(40,sum.getSum(),THRESHOLD);

    for (int i=0; i<20; i+=2){
      sum.setSummandActive(i, false);
    }
    assertEquals(20,sum.getSum(),THRESHOLD);

    // summands that haven't been set may be inactivated
    sum.setSummandActive(9999, false);
    assertEquals(20,sum.getSum(),THRESHOLD);
    sum.setSummand(9999, 321498237);
    assertEquals(20,sum.getSum(),THRESHOLD);

    // setting unset summands has no effect
    sum.setSummandActive(98982398, true);
    assertEquals(20,sum.getSum(),THRESHOLD);
    
    // change inactive summands
    for (int i=0; i<20; i+=2){
      sum.setSummand(i, 1);
    }
    // no change
    assertEquals(20,sum.getSum(),THRESHOLD);

    // now activate the changed summands
    for (int i=0; i<20; i+=2){
      sum.setSummandActive(i, true);
    }
    assertEquals(30,sum.getSum(),THRESHOLD);
  }

}
