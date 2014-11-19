/**
 * Copyright 2014 Brigham Young University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.byu.nlp.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author rah67
 *
 */
public class DoubleArrayAveragerTest {

  private static final double TOL=1e-6;
  
  DoubleArrayAverager avg;
  
  @Before
  public void setup(){
    avg = new DoubleArrayAverager(5);
    avg.increment(new double[]{0, .5, -10, 10.5});
    avg.increment(new double[]{1, .5, -5,  10.6});
    avg.increment(new double[]{2, .5, 5,   10.7});
    avg.increment(new double[]{3, .5, 10,  10.8});
  }
  
  @Test
  public void testValues() {
    double[] vals = avg.average();
    Assert.assertEquals(vals[0], 1.5, TOL);
    Assert.assertEquals(vals[1], .5, TOL);
    Assert.assertEquals(vals[2], 0, TOL);
    Assert.assertEquals(vals[3], 10.65, TOL);
    Assert.assertEquals(vals[4], 0, TOL);
  }

}
