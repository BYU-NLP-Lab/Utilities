/**
 * Copyright 2011 Brigham Young University
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

import org.apache.commons.math3.linear.RealVector;

/**
 * A categorical distribution whose parameters are stored in a vector. The parameters represent the log probability of
 * each possible event.
 * 
 * @author rah67
 *
 */
public class VectorCategoricalDistribution implements CategoricalDistribution {

	private final RealVector logProbs;

	/**
	 * Constructs a new categorical distribution using the logProbs vector as parameters. This class assumes ownership
	 * of the vector (i.e., no copy is made). The parameters should be in log space, otherwise, invoke the
	 * constructor as follows:
	 * 
	 *  {@code
	 *     new VectorCategoricalDistribution(scores.mapLogToSelf());
	 *  }
	 *  
	 *  No verification is performed to ensure a proper distribution.
	 */
	public VectorCategoricalDistribution(RealVector logProbs) {
		this.logProbs = logProbs;
	}

    /** {@inheritDoc} */
	@Override
	public double logProbabilityOf(int event) {
		return logProbs.getEntry(event);
	}

    /** {@inheritDoc} */
	@Override
	public int argMax() {
		return logProbs.getMaxIndex();
	}

    /** {@inheritDoc} */
    @Override
    public double logMax() {
        return logProbs.getMaxValue();
    }

    /** {@inheritDoc} */
    @Override
    public double entropy() {
        double entropy = 0.0;
        // TODO(rhaertel): when Apache fixes their iterator, then use that.
        for (int i = 0; i < logProbs.getDimension(); i++) {
            double logProb = logProbs.getEntry(i);
            if (logProb > Double.NEGATIVE_INFINITY) {
                entropy -= Math.exp(logProb) * logProb;
            }
        }
        return entropy;
    }
}
