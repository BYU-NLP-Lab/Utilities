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

import org.apache.commons.math3.random.RandomGenerator;

/**
 * @author rah67
 *
 */
public class UniformDistribution implements CategoricalDistribution {

    private final int numLabels;
    private final RandomGenerator rnd;

    public UniformDistribution(int numLabels, RandomGenerator rnd) {
        this.numLabels = numLabels;
        this.rnd = rnd;
    }

    /** {@inheritDoc} */
    @Override
    public double logProbabilityOf(int event) {
        return 1.0 / numLabels;
    }

    /** {@inheritDoc} */
    @Override
    public int argMax() {
        return rnd.nextInt(numLabels);
    }

    /** {@inheritDoc} */
    @Override
    public double logMax() {
        return -Math.log(numLabels);
    }

    /** {@inheritDoc} */
    @Override
    public double entropy() {
        return Math.log(numLabels);
    }

}
