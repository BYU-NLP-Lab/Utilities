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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

import edu.byu.nlp.math.Math2;
import edu.byu.nlp.util.DoubleArrays;


/**
 * A categorical distribution whose parameters are stored in an array of doubles. The parameters represent the log
 * probability of each possible event. Slightly more efficient for some operations than
 * {@code VectorCategoricalDistribution}.
 * 
 * @author rah67
 *
 */
public class DoubleArrayCategoricalDistribution implements CategoricalDistribution {

	private final double[] logProbs;

    /**
     * Given the specified parameters, creates a new distribution, making copies of the parameters, if specified.
     */
    public static CategoricalDistribution newDistributionFromProbs(double[] probs, boolean copy) {
        Preconditions.checkNotNull(probs);
        if (copy) {
            probs = probs.clone();
        }
        DoubleArrays.logToSelf(probs);
        return new DoubleArrayCategoricalDistribution(probs);
    }
    
    /**
     * Given the specified parameters, creates a new distribution, making copies of the parameters, if specified. An
     * IllegalArgumentException is thrown if the distribution doesn't sum to 1.0 within the specified tolerance.
     */
    public static CategoricalDistribution newDistributionFromProbs(double[] probs, boolean copy, double tolerance) {
        Preconditions.checkNotNull(probs);
        if (copy) {
            probs = probs.clone();
        }
        double sum = 0.0;
        for (int i = 0; i < probs.length; i++) {
            sum += probs[i];
        }
        if (!Math2.doubleEquals(sum, 1.0, tolerance)) {
            throw new IllegalArgumentException("Not a proper distribution; sum = " + sum);
        }
        DoubleArrays.logToSelf(probs);
        return new DoubleArrayCategoricalDistribution(probs);
    }
    
    /**
     * Given the parameters in log space, creates a new distribution, making copies of the parameters, if specified.
     */
	public static CategoricalDistribution newDistributionFromLogProbs(double[] logProbs, boolean copy) {
        Preconditions.checkNotNull(logProbs);
        if (copy) {
            logProbs = logProbs.clone();
        }
	    return new DoubleArrayCategoricalDistribution(logProbs);
	}
	
	/**
	 * Given parameters in log space, creates a new distribution, making copies of the parameters, if specified. An
	 * IllegalArgumentException is thrown if the distribution doesn't sum to 1.0 within the specified tolerance.
	 */
    public static CategoricalDistribution newDistributionFromLogProbs(double[] logProbs, boolean copy,
                                                                      double tolerance) {
        Preconditions.checkNotNull(logProbs);
        if (copy) {
            logProbs = logProbs.clone();
        }
        double logSum = DoubleArrays.logSum(logProbs);
        if (!Math2.doubleEquals(logSum, 0.0, tolerance)) {
            throw new IllegalArgumentException(
                    "Not a proper distribution; log(sum) = " + logSum + "; sum = " + Math.exp(logSum));
        }
        return new DoubleArrayCategoricalDistribution(logProbs);
    }
    
	/**
	 * Constructs a new categorical distribution using the logProbs vector as parameters. This class assumes ownership
	 * of the array (i.e., no copy is made). The parameters should be in log space.
	 *  
	 *  No verification is performed to ensure a proper distribution.
	 */
	@VisibleForTesting DoubleArrayCategoricalDistribution(double[] logProbs) {
		this.logProbs = logProbs;
	}

    /** {@inheritDoc} */
	@Override
	public double logProbabilityOf(int event) {
		return logProbs[event];
	}

    /** {@inheritDoc} */
	@Override
	public int argMax() {
		return DoubleArrays.argMax(logProbs);
	}

    /** {@inheritDoc} */
    @Override
    public double logMax() {
        return DoubleArrays.max(logProbs);
    }

    /** {@inheritDoc} */
    @Override
    public double entropy() {
        double entropy = 0.0;
        // TODO(rhaertel): when Apache fixes their iterator, then use that.
        for (int i = 0; i < logProbs.length; i++) {
            double logProb = logProbs[i];
            if (logProb > Double.NEGATIVE_INFINITY) {
                entropy -= Math.exp(logProb) * logProb;
            }
        }
        return entropy;
    }
}
