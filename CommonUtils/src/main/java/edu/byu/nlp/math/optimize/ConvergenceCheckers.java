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
package edu.byu.nlp.math.optimize;

import com.google.common.base.Preconditions;

/**
 * @author rah67
 *
 */
public class ConvergenceCheckers {

	private ConvergenceCheckers() { }
	
	public static ConvergenceChecker or(final ConvergenceChecker a, final ConvergenceChecker b) {
		Preconditions.checkNotNull(a);
		Preconditions.checkNotNull(b);
		return new ConvergenceChecker() {

			@Override
			public boolean isConverged(int iteration, double prev, double cur) {
				return a.isConverged(iteration, prev, cur) || b.isConverged(iteration, prev, cur);
			}
			
		};
	}
	
	public static ConvergenceChecker and(final ConvergenceChecker a, final ConvergenceChecker b) {
		Preconditions.checkNotNull(a);
		Preconditions.checkNotNull(b);
		return new ConvergenceChecker() {

			@Override
			public boolean isConverged(int iteration, double prev, double cur) {
				return a.isConverged(iteration, prev, cur) && b.isConverged(iteration, prev, cur);
			}
			
		};
	}
	
	public static ConvergenceChecker not(final ConvergenceChecker checker) {
		Preconditions.checkNotNull(checker);
		return new ConvergenceChecker() {

			@Override
			public boolean isConverged(int iteration, double prev, double cur) {
				return !isConverged(iteration, prev, cur);
			}
			
		};
	}
	
	public static ConvergenceChecker maxIterations(final int maxIterations) {
		Preconditions.checkArgument(maxIterations > 0, "maxIterations (%s) must be greater than 0", maxIterations);
		return new ConvergenceChecker() {

			@Override
			public boolean isConverged(int iteration, double prev, double cur) {
				return iteration >= maxIterations;
			}
			
		};
	}
	
	private static class RelativePercentChangeConvergenceChecker implements ConvergenceChecker {

		private final double tolerance;
		
		public RelativePercentChangeConvergenceChecker(double tolerance) {
			this.tolerance = tolerance;
		}
		
		/** {@inheritDoc} */
		@Override
		public boolean isConverged(int iteration, double prev, double cur) {
			return relativePercentChange(prev, cur) < tolerance;
		}
		
		private static double relativePercentChange(double prev, double cur) {
			if (Double.isInfinite(prev) && Double.isInfinite(cur)) {
				throw new IllegalArgumentException("Previous and current values are infinite");
			}
			if (Double.isNaN(prev)) {
				throw new IllegalArgumentException("Previous value is not a number");
			}
			if (Double.isNaN(cur)) {
				throw new IllegalArgumentException("Current value is not a number");
				
			}
			if (Double.isInfinite(prev) || Double.isInfinite(cur)) {
				return Double.POSITIVE_INFINITY;
			}

			return 2.0 * Math.abs((prev - cur) / (prev + cur));
		}

	}

	public static ConvergenceChecker relativePercentChange(double tolerance) {
		Preconditions.checkArgument(tolerance > 0.0, "tolerance (%s) must be strictly greater than 0.0", tolerance);
		return new RelativePercentChangeConvergenceChecker(tolerance);
	}
}
