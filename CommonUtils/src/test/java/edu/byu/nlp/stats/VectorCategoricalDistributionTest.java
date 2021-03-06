/**
 * Copyright 2012 Brigham Young University
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

import static java.lang.Math.log;
import static org.fest.assertions.Assertions.assertThat;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.fest.assertions.Delta;
import org.junit.Test;

/**
 * @author rah67
 *
 */
public class VectorCategoricalDistributionTest {

    /**
     * Test method for {@link edu.byu.nlp.stats.VectorCategoricalDistribution#entropy()}.
     */
    @Test
    public void testEntropy() {
        double[] logProbs = new double[]{ log(0.7), log(0.0), log(0.3) };
        VectorCategoricalDistribution dist = new VectorCategoricalDistribution(new ArrayRealVector(logProbs));
        assertThat(dist.entropy()).isEqualTo(-0.7 * log(0.7) - 0.3 * log(0.3), Delta.delta(1e-10));
    }
    
    @Test
    public void testLogMax() {
        double[] logProbs = new double[]{ log(0.7), log(0.0), log(0.3) };
        VectorCategoricalDistribution dist = new VectorCategoricalDistribution(new ArrayRealVector(logProbs));
        assertThat(dist.logMax()).isEqualTo(logProbs[0]);
        // Tests for consisteny behavior
        assertThat(dist.logMax()).isEqualTo(dist.logProbabilityOf(dist.argMax()), Delta.delta(1e-14));
    }

    @Test
    public void testArgMax() {
        double[] logProbs = new double[]{ log(0.7), log(0.0), log(0.3) };
        VectorCategoricalDistribution dist = new VectorCategoricalDistribution(new ArrayRealVector(logProbs));
        assertThat(dist.argMax()).isEqualTo(0);
    }

    @Test
    public void testLogProbabilityOf() {
        double[] logProbs = new double[]{ log(0.7), log(0.0), log(0.3) };
        VectorCategoricalDistribution dist = new VectorCategoricalDistribution(new ArrayRealVector(logProbs));
        assertThat(dist.logProbabilityOf(0)).isEqualTo(logProbs[0], Delta.delta(1e-14));
        assertThat(dist.logProbabilityOf(1)).isEqualTo(Double.NEGATIVE_INFINITY, Delta.delta(1e-14));
        assertThat(dist.logProbabilityOf(2)).isEqualTo(logProbs[2], Delta.delta(1e-14));
        
        try {
            dist.logProbabilityOf(3);
        } catch(Exception expected) {}  // TODO(rhaertel): more specific exception
    }
}
