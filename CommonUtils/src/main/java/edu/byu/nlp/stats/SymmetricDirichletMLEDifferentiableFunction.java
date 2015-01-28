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

import org.apache.commons.math3.analysis.DifferentiableMultivariateFunction;
import org.apache.commons.math3.analysis.DifferentiableMultivariateVectorFunction;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;

import com.google.common.base.Preconditions;

/**
 * @author rah67
 *
 */
public class SymmetricDirichletMLEDifferentiableFunction implements DifferentiableMultivariateFunction {

//	private static final Logger logger = LoggerFactory.getLogger(SymmetricDirichletMLEDifferentiableFunction.class);
	
	private final SymmetricDirichletOptimizationHelper helper;

	public static SymmetricDirichletMLEDifferentiableFunction newDifferentiableFunction(double[][] data) {
		SymmetricDirichletOptimizationHelper helper =
				SymmetricDirichletOptimizationHelper.newHelper(data);
		return new SymmetricDirichletMLEDifferentiableFunction(helper);
	}
	
	private SymmetricDirichletMLEDifferentiableFunction(
			SymmetricDirichletOptimizationHelper newDifferentiableFunction) {
		this.helper = newDifferentiableFunction;
	}

	/** {@inheritDoc} */
	@Override
	public double value(double[] x) {
		Preconditions.checkNotNull(x);
		Preconditions.checkArgument(x.length == 1);
		return helper.valueAt(x[0]);
	}
	
	private DifferentiableMultivariateVectorFunction gradient;

	/** {@inheritDoc} */
	@Override
	public DifferentiableMultivariateVectorFunction gradient() {
		// Lazy instantiation
		if (gradient == null) {
			gradient = new Gradient();
		}
		return gradient;
	}

	private DifferentiableMultivariateFunction partialDerivative;
	
	/** {@inheritDoc} */
	@Override
	public DifferentiableMultivariateFunction partialDerivative(int k) {
		Preconditions.checkArgument(k == 0);
		
		// Lazy instantiation
		if (partialDerivative == null) {
			partialDerivative = new PartialDerivative();
		}
		return partialDerivative;
	}

	private class Gradient implements DifferentiableMultivariateVectorFunction {
		
		@Override
		public double[] value(double[] x) throws IllegalArgumentException {
			Preconditions.checkNotNull(x);
			Preconditions.checkArgument(x.length == 1);
			return new double[]{ helper.firstDerivativeAt(x[0]) };
		}

		private MultivariateMatrixFunction jacobian;
		
		/** {@inheritDoc} */
		@Override
		public MultivariateMatrixFunction jacobian() {
			// Lazy instantiation
			if (jacobian == null) {
				jacobian = new Jacobian();
			}
			return jacobian;
		}

	}
	
	private class Jacobian implements MultivariateMatrixFunction {

		/** {@inheritDoc} */
		@Override
		public double[][] value(double[] x) throws IllegalArgumentException {
			Preconditions.checkNotNull(x);
			Preconditions.checkArgument(x.length == 1);
			return new double[][]{ { helper.secondDerivativeAt(x[0]) } };
		}
		
	}

	private class PartialDerivative implements DifferentiableMultivariateFunction {

		/** {@inheritDoc} */
		@Override
		public double value(double[] x) {
			Preconditions.checkNotNull(x);
			Preconditions.checkArgument(x.length == 1);
			return helper.firstDerivativeAt(x[0]);
		}

		private MultivariateVectorFunction gradient;
		
		/** {@inheritDoc} */
		@Override
		public MultivariateVectorFunction gradient() {
			// Lazy instantiation
			if (gradient == null) {
				gradient = new SecondGradient();
			}
			return gradient;
		}

		private MultivariateFunction partial;
		
		/** {@inheritDoc} */
		@Override
		public MultivariateFunction partialDerivative(int k) {
			Preconditions.checkArgument(k == 0);

			if (partial == null) {
				partial = new SecondPartialDerivative();
			}
			return partial;
		}
		
	}
	
	private class SecondGradient implements MultivariateVectorFunction {

		/** {@inheritDoc} */
		@Override
		public double[] value(double[] x) throws IllegalArgumentException {
			Preconditions.checkNotNull(x);
			Preconditions.checkArgument(x.length == 1);
			return new double[]{ helper.secondDerivativeAt(x[0]) };
		}
		
	}
	
	private class SecondPartialDerivative implements MultivariateFunction {

		/** {@inheritDoc} */
		@Override
		public double value(double[] x) {
			Preconditions.checkNotNull(x);
			Preconditions.checkArgument(x.length == 1);
			return helper.secondDerivativeAt(x[0]);
		}
		
	}
	
}
