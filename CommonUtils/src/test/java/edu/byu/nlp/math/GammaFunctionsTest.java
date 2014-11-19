/**
 * Copyright 2012 Brigham Young University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.byu.nlp.math;

import static java.lang.Math.log;
import static org.fest.assertions.Assertions.assertThat;

import org.apache.commons.math3.special.Gamma;
import org.fest.assertions.Delta;
import org.junit.Test;

/**
 * @author rah67
 * 
 */
public class GammaFunctionsTest {

  /**
   * Test method for {@link edu.byu.nlp.math.GammaFunctions#logRatioOfGammas(double, double)}.
   */
  @Test
  public void testLogRatioOfGammas() {
    // integer tests adapted from testlogRisingFactorial
    Delta d = Delta.delta(1e-10);
    assertThat(GammaFunctions.logRatioOfGammas(10.3, 10.3)).isEqualTo(log(1.0), d);
    assertThat(GammaFunctions.logRatioOfGammas(10.3 + 1, 10.3)).isEqualTo(log(10.3), d);
    assertThat(GammaFunctions.logRatioOfGammas(10.3 + 2, 10.3)).isEqualTo(log(10.3 * 11.3), d);
    assertThat(GammaFunctions.logRatioOfGammas(10.3 + 3, 10.3)).isEqualTo(log(10.3 * 11.3 * 12.3),
        d);
    assertThat(GammaFunctions.logRatioOfGammas(10.3 + 4, 10.3)).isEqualTo(
        log(10.3 * 11.3 * 12.3 * 13.3), d);

    assertThat(GammaFunctions.logRatioOfGammas(1.3 + 10, 1.3)).isEqualTo(
        log(1.3 * 2.3 * 3.3 * 4.3 * 5.3 * 6.3 * 7.3 * 8.3 * 9.3 * 10.3), d);

    // TODO: fractional tests
  }

  /**
   * Test method for {@link edu.byu.nlp.math.GammaFunctions#logRisingFactorial(double, int)}.
   */
  @Test
  public void testlogRisingFactorial() {
    Delta d = Delta.delta(1e-10);
    assertThat(GammaFunctions.logRisingFactorial(10.3, 0)).isEqualTo(log(1.0), d);
    assertThat(GammaFunctions.logRisingFactorial(10.3, 1)).isEqualTo(log(10.3), d);
    assertThat(GammaFunctions.logRisingFactorial(10.3, 2)).isEqualTo(log(10.3 * 11.3), d);
    assertThat(GammaFunctions.logRisingFactorial(10.3, 3)).isEqualTo(log(10.3 * 11.3 * 12.3), d);
    assertThat(GammaFunctions.logRisingFactorial(10.3, 4))
        .isEqualTo(log(10.3 * 11.3 * 12.3 * 13.3), d);

    // We want at least one test to be computed manually and we will compare it to the
    // difference in Gamma method.
    int threshold = GammaFunctions.MANUALLY_COMPUTE_RISING_FACTORIAL_THRESHOLD;
    if (threshold > 0) {
      assertThat(GammaFunctions.logRisingFactorial(1.3, threshold - 1))
          .isEqualTo(Gamma.logGamma(1.3 + threshold - 1) - Gamma.logGamma(1.3), d);
    }

    // A test that is not computed manually.
    assertThat(GammaFunctions.logRisingFactorial(1.3, threshold + 100))
        .isEqualTo(Gamma.logGamma(1.3 + threshold + 100) - Gamma.logGamma(1.3), d);
  }

  @Test(expected = Exception.class)
  public void testlogRisingFactorialPreconditionsNegativeDiff() {
    GammaFunctions.logRisingFactorial(1, -1);
  }

  @Test(expected = Exception.class)
  public void testlogRisingFactorialPreconditionNegativeX() {
    GammaFunctions.logRisingFactorial(-1, 0);
  }
}
