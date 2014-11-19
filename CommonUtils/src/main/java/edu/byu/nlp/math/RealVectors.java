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
package edu.byu.nlp.math;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.linear.RealVector;

import com.google.common.base.Preconditions;

import edu.byu.nlp.util.DoubleArrays;

/**
 * Extension and utility methods for RealVectors.
 * 
 * @author rah67
 *
 */
public class RealVectors {

	private RealVectors() {}
	
	/**
	 * Adds vector a to b and stores the results in a.
	 * 
	 * @throws NullPointerException if a or b are null 
	 */
	public static void addToSelf(RealVector a, RealVector b) {
		Preconditions.checkNotNull(a);
		Preconditions.checkNotNull(b);
		if (a.getDimension() != b.getDimension()) {
			throw new DimensionMismatchException(b.getDimension(), a.getDimension());
		}
		
		a.combineToSelf(1.0, 1.0, b);
	}
	
	/**
	 * Computes the log of the sum of the exponentiated elements of a vector. For any index j:
	 * 
	 * <pre>
	 * log(e^{x_1} + e^{x_2} + ...) = log(e^{x_j} * sum\_{i \neq j} e^{x_i - x_j} + 1)
     *                              = x_j + log(sum\_{i \neq j} e^{x_i - x_j} + 1)
     * </pre>
     * 
     * This method ignores elements that are twenty orders of magnitude different than x_j.
     * 
     * @throws NullPointerException if vector is null
     */
	public static double logSumSloppy(RealVector x) {
		Preconditions.checkNotNull(x);
		
		// TODO(rah67): consider just using the first element
        // use max as j
        int argMax = x.getMaxIndex();
        double max = x.getEntry(argMax);
        
        if (max == Double.NEGATIVE_INFINITY)
            return Double.NEGATIVE_INFINITY;
        
        return DoubleArrays.logSum(x.toArray());
	}

	/**
	 * Normalizes a distribution in log space. Assuming each vector element is in the range (-\infty, 0] (this condition
	 * is unchecked), then this method computes log(e^{x_i} / \sum_j e^{x_j}) for each i. The original entries are
	 * replaced. Returns the original vector.
	 */
	public static RealVector logNormalizeToSelf(RealVector scores) {
		double logZ = RealVectors.logSumSloppy(scores);
		return scores.mapSubtractToSelf(logZ);
	}

	/** Incomplete implementation **/ 
	public static RealVector constant(final double value, final int dimension) {
		return new RealVector() {

			@Override
			public int getDimension() {
				return dimension;
			}

			@Override
			public double getEntry(int index) {
				return value;
			}

			@Override
			public RealVector append(RealVector arg0) {
				throw new UnsupportedOperationException();
			}

			@Override
			public RealVector append(double arg0) {
				throw new UnsupportedOperationException();
			}

			@Override
			public RealVector copy() {
				throw new UnsupportedOperationException();
			}

			@Override
			public RealVector ebeDivide(RealVector arg0) {
				throw new UnsupportedOperationException();
			}

			@Override
			public RealVector ebeMultiply(RealVector arg0) {
				throw new UnsupportedOperationException();
			}

			@Override
			public RealVector getSubVector(int arg0, int arg1) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean isInfinite() {
				return false;
			}

			@Override
			public boolean isNaN() {
				return false;
			}

			@Override
			public RealVector projection(RealVector arg0) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void setEntry(int arg0, double arg1) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void setSubVector(int arg0, RealVector arg1) {
				throw new UnsupportedOperationException();
			}
			
		};
	}

	public static void incrementEntry(RealVector v, int index) {
		v.setEntry(index, v.getEntry(index) + 1);
	}
	
	public static void decrementEntry(RealVector v, int index) {
		v.setEntry(index, v.getEntry(index) - 1);
	}
	
}
