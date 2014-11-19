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

import org.apache.commons.math3.analysis.DifferentiableMultivariateFunction;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;

import edu.byu.nlp.util.Caches;
import edu.byu.nlp.util.ValueSupplier;

/**
 * A {@link MultivariateRealFunction} that computes the value and gradient at the same time and caches the result
 * between invocations of {@code value()} and {@code gradient()}. Useful when there is overlap in the computation of the
 * value and gradient that is non-trivial computationally.
 * 
 * @author rah67
 *
 */
public class CachedDifferentiableMultivariateFunction implements DifferentiableMultivariateFunction {

	public static class ValueAndGradient {
		private final double value;
		private final double[] gradient;
		
		public ValueAndGradient(double value, double[] gradient) {
			this.value = value;
			this.gradient = gradient;
		}
		
		public double getValue() { return value; }
		public double[] getGradient() { return gradient; }
	}
	
	private class Gradient implements MultivariateVectorFunction {
		@Override
		public double[] value(double[] x) {
			return cachedValueSupplier.get(x).getGradient();
		}
	}
	
	private final ValueSupplier<double[], ValueAndGradient> cachedValueSupplier;
	private final MultivariateVectorFunction gradient;
	
	/**
	 * @param valueSupplier supplies values on a cache miss.
	 */
	public CachedDifferentiableMultivariateFunction(
			ValueSupplier<double[], ValueAndGradient> valueSupplier) {
		cachedValueSupplier = Caches.lastValueReferenceCache(valueSupplier);
		gradient = new Gradient();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public double value(double[] x) {
		return cachedValueSupplier.get(x).getValue();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MultivariateVectorFunction gradient() {
		return gradient;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MultivariateFunction partialDerivative(final int i) {
		return new MultivariateFunction() {
			@Override
			public double value(double[] x) {
				return cachedValueSupplier.get(x).getGradient()[i];
			}
		};
	}
}
