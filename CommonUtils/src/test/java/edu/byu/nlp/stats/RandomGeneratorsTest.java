/**
 * Copyright 2013 Brigham Young University
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
package edu.byu.nlp.stats;

import static org.fest.assertions.Assertions.assertThat;

import java.util.Arrays;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.special.Gamma;
import org.junit.Test;

import edu.byu.nlp.util.DoubleArrays;
import edu.byu.nlp.util.IntArrays;

/**
 * @author rah67
 *
 */
public class RandomGeneratorsTest {

  /**
   * Dirichlet Compounad Multinomial where \alpha_k = 1 for all k. 
   * 
   * NOTE: this DCM is based on iid samples from a Categorical distribution and hence lacks the
   * multinomial factorial.
   */
  // TODO(rhaertel): Implement a DCM class and just call that
  private double dcmLogDensityWithUniformPrior(int[] observations) {
    double sumAlpha = observations.length;
    double logDensity = Gamma.logGamma(sumAlpha) -
        Gamma.logGamma(IntArrays.sum(observations) + sumAlpha);
    for (int i = 0; i < observations.length; i++) {
      logDensity += Gamma.logGamma(observations[i] + 1.0) - Gamma.logGamma(1.0);
    }
    return logDensity;
  }
  
  /**
   * Tests the property that, with a sufficient number of samples, the empirical distribution
   * of the samples approaches that specified by the parameters.
   * 
   * The methodology used is Bayesian hypothesis testing. It is true that a utility function could
   * have been used here (e.g., log-loss \equivto K-L divergence), but then we would have some
   * arbitrary threshold we would have to set as an acceptable metric. Instead, we can use
   * the arbitrary scale for Bayes factory given by Jeffreys.
   * 
   * The hypothesis is designed as such. H1 states that the observed data was generated using the
   * provided parameters \hat{\theta}, i.e., p(\theta | H1) = 1 iff \theta = \hat{\theta}. This is
   * to be interpreted as meaning that the code correctly samples from the provided distribution.
   * H2 is that all parameters are equally likely a-priori, i.e., \theta | H2 ~ uniform Dirichlet.
   * Should this hypothesis be more likely than the previous hypothesis, there is a bug. 
   * 
   * Assuming a uniform prior over H1 and H2, then the posterior probability of the respective
   * hypotheses after observing x (a count vector) are: p(H1 | x) \propto p(x|H1) and
   * p(H2 | x) \propto p(x | H2). Thus, we can use the Bayes Factor, p(x | H1) / p(x | H2) to
   * compare hypotheses. According to Jeffrey's scale, a ratio of 100:1 would give decisive support
   * to the conclusion that there are no bugs.
   * 
   * Note that there is a relationship between the number of samples we draw, the dimensionality
   * of the parameter vector, and the strength of the factor.
   * 
   * With enough samples then the Bayes factor will either be much greater than 100 (no bugs) or
   * much less than 1.0/100.0 (bugs).
   * 
   * See http://idiom.ucsd.edu/~rlevy/lign251/fall2007/lecture_9.pdf.
   */
  public double logBayesFactor(int[] counts, double[] thetaHat) {
    assertThat(counts.length).isEqualTo(thetaHat.length);
    
    // p(x | H1) = \int p(\theta | H1) \prod_i p(x_i | \theta) d\theta
    //           = \prod_i p(x_i | \hat{\theta})
    double logPOfXGivenH1 = 0.0;
    for (int i = 0; i < counts.length; i++) {
      logPOfXGivenH1 += counts[i] * Math.log(thetaHat[i]);
    }
    
    // p(x | H2) = \int p(\theta | H2) \prod_i p(x_i | \theta) d\theta
    // x | H2 ~ UniformDCM
    double logPOfXGivenH2 = dcmLogDensityWithUniformPrior(counts);
    return logPOfXGivenH1 - logPOfXGivenH2; 
  }
  
  /**
   * Test method for {@link edu.byu.nlp.stats.RandomGenerators#nextIntUnnormalizedProbs(org.apache.commons.math3.random.RandomGenerator, double[])}.
   */ 
  @Test
  public void testNextIntUnnormalizedProbsWithBinarySearch() {
    final int numSamples = 10000;
    final double[] theta = new double[]{ 0.1, 0.1, 0.1, 0.2, 0.1, 0.1, 0.1, 0.1, 0.1 };
    
    // Ensure binary search will kick in
    assertThat(theta.length).isGreaterThan(RandomGenerators.BINARY_SEARCH_THRESHOLD);
    
    // Convert the parameters to an unnormalized log prob.
    double[] unnormalizedProbs = theta.clone();
    DoubleArrays.multiplyToSelf(unnormalizedProbs, 1234);
    
    RandomGenerator rnd = new MersenneTwister();
    
    // Sample and count how often each event occurred.
    int[] counts = new int[theta.length];
    for (int i = 0; i < numSamples; i++) {
      ++counts[RandomGenerators.nextIntUnnormalizedProbs(rnd, unnormalizedProbs)];
    }

    assertThat(logBayesFactor(counts, theta)).isGreaterThanOrEqualTo(Math.log(100.0));
  }

  /**
   * Test method for {@link edu.byu.nlp.stats.RandomGenerators#nextIntUnnormalizedProbs(org.apache.commons.math3.random.RandomGenerator, double[])}.
   */
  @Test
  public void testNextIntUnnormalizedProbsWithLinearSearch() {
    final int numSamples = 10000;
    final double[] theta = new double[]{ 0.3, 0.3, 0.4 };
    
    // Ensure linear search will be used.
    assertThat(theta.length).isLessThan(RandomGenerators.BINARY_SEARCH_THRESHOLD);
    
    // Convert the parameters to an unnormalized log prob.
    double[] unnormalizedProbs = theta.clone();
    DoubleArrays.multiplyToSelf(unnormalizedProbs, 1234);
    
    RandomGenerator rnd = new MersenneTwister();
    
    // Sample and count how often each event occurred.
    int[] counts = new int[theta.length];
    for (int i = 0; i < numSamples; i++) {
      ++counts[RandomGenerators.nextIntUnnormalizedProbs(rnd, unnormalizedProbs)];
    }
    System.out.println(Arrays.toString(counts));
    assertThat(logBayesFactor(counts, theta)).isGreaterThanOrEqualTo(Math.log(100.0));
  }

  /**
   * Test method for {@link edu.byu.nlp.stats.RandomGenerators#nextIntUnnormalizedLogProbs(org.apache.commons.math3.random.RandomGenerator, double[])}.
   */
  @Test
  public void testNextIntUnnormalizedLogProbsWithBinarySearch() {
    final int numSamples = 10000;
    final double[] theta = new double[]{ 0.1, 0.1, 0.1, 0.2, 0.1, 0.1, 0.1, 0.1, 0.1 };
    
    // Ensure binary search will kick in
    assertThat(theta.length).isGreaterThan(RandomGenerators.BINARY_SEARCH_THRESHOLD);
    
    // Convert the parameters to an unnormalized log prob.
    double[] unnormalizedLogProbs = DoubleArrays.log(theta);
    DoubleArrays.addToSelf(unnormalizedLogProbs, -1234);
    
    RandomGenerator rnd = new MersenneTwister();
    
    // Sample and count how often each event occurred.
    int[] counts = new int[theta.length];
    for (int i = 0; i < numSamples; i++) {
      ++counts[RandomGenerators.nextIntUnnormalizedLogProbs(rnd, unnormalizedLogProbs)];
    }

    assertThat(logBayesFactor(counts, theta)).isGreaterThanOrEqualTo(Math.log(100.0));
  }

  /**
   * Test method for {@link edu.byu.nlp.stats.RandomGenerators#nextIntUnnormalizedLogProbs(org.apache.commons.math3.random.RandomGenerator, double[])}.
   */
  @Test
  public void testNextIntUnnormalizedLogProbsWithLinearSearch() {
    final int numSamples = 10000;
    final double[] theta = new double[]{ 0.3, 0.3, 0.4 };
    
    // Ensure linear search will be used.
    assertThat(theta.length).isLessThan(RandomGenerators.BINARY_SEARCH_THRESHOLD);

    // Convert the parameters to an unnormalized log prob.
    double[] unnormalizedLogProbs = DoubleArrays.log(theta);
    DoubleArrays.addToSelf(unnormalizedLogProbs, -1234);
    
    RandomGenerator rnd = new MersenneTwister();
    
    // Sample and count how often each event occurred.
    int[] counts = new int[theta.length];
    for (int i = 0; i < numSamples; i++) {
      ++counts[RandomGenerators.nextIntUnnormalizedLogProbs(rnd, unnormalizedLogProbs)];
    }

    assertThat(logBayesFactor(counts, theta)).isGreaterThanOrEqualTo(Math.log(100.0));
  }
}
