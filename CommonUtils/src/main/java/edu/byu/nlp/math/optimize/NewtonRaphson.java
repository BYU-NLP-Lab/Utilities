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

import org.apache.commons.math3.analysis.DifferentiableMultivariateFunction;
import org.apache.commons.math3.analysis.DifferentiableMultivariateVectorFunction;
import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.optimization.ConvergenceChecker;
import org.apache.commons.math3.optimization.DifferentiableMultivariateOptimizer;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.PointValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import edu.byu.nlp.util.DoubleArrays;

/**
 * @author rah67
 *
 */
public class NewtonRaphson implements DifferentiableMultivariateOptimizer {

	private static final Logger logger = LoggerFactory.getLogger(NewtonRaphson.class);
	
	private final ConvergenceChecker<PointValuePair> convergenceChecker;
	private final int maxEvaluations;
	private int evaluations;
	
	public NewtonRaphson(ConvergenceChecker<PointValuePair> convergenceChecker, int maxEvaluations) {
		this.convergenceChecker = convergenceChecker;
		this.maxEvaluations = maxEvaluations;
		this.evaluations = 0;
	}

	/** {@inheritDoc} */
	@Override
	public PointValuePair optimize(int maxEval, DifferentiableMultivariateFunction f, GoalType goalType,
			double[] initial) {
		Preconditions.checkArgument(maxEval > 0);
		Preconditions.checkNotNull(f);
		Preconditions.checkNotNull(goalType);
		Preconditions.checkNotNull(initial);
		
		// FIXME : goalType is being ignored!
		
		Updater updater = Updater.newUpdater(f);
		
		PointValuePair prev = null;
		PointValuePair next = updater.update(initial);
		logger.info(String.format("Iteration 0, value = %f", next.getValue()));

		evaluations = 1;
		do {
			prev = next;
			double[] x = prev.getPointRef();
			next = updater.update(x);
			++evaluations;
			logger.info(String.format("Iteration %d, value = %f", evaluations, next.getValue()));
		} while (!convergenceChecker.converged(evaluations, prev, next) && evaluations < maxEval 
				&& evaluations < maxEvaluations);
		
		return next;
	}
	
	private static class Updater {
		
		private final DifferentiableMultivariateFunction f;
		private final DifferentiableMultivariateVectorFunction gradient;
		private final MultivariateMatrixFunction jacobian;
		
		public static Updater newUpdater(DifferentiableMultivariateFunction f) {
			if (!(f.gradient() instanceof DifferentiableMultivariateVectorFunction)) {
				throw new IllegalArgumentException("Gradient must be differentiable!");
			}
			
			DifferentiableMultivariateVectorFunction gradient = (DifferentiableMultivariateVectorFunction) f.gradient();
			MultivariateMatrixFunction jacobian = gradient.jacobian();
			return new Updater(f, gradient, jacobian);
		}
		
		private Updater(DifferentiableMultivariateFunction f, DifferentiableMultivariateVectorFunction gradient,
				MultivariateMatrixFunction jacobian) {
			this.f = f;
			this.gradient = gradient;
			this.jacobian = jacobian;
		}

		// X is changed in place; PointValuePair contains a reference to x
		private PointValuePair update(double[] x) {
			// x^new = x^old - h(x)^-1 g(x)
			double[] g = gradient.value(x);
			double[][] hessian = jacobian.value(x);
			double[] offset =
					new LUDecomposition(new Array2DRowRealMatrix(hessian, false)).getSolver().getInverse().preMultiply(g);
			DoubleArrays.subtractToSelf(x, offset);
			
			return new PointValuePair(x, f.value(x), false);
		}

	}
	
	/** {@inheritDoc} */
	@Override
	public ConvergenceChecker<PointValuePair> getConvergenceChecker() {
		return convergenceChecker;
	}

	/** {@inheritDoc} */
	@Override
	public int getEvaluations() {
		return evaluations;
	}

	/** {@inheritDoc} */
	@Override
	public int getMaxEvaluations() {
		return maxEvaluations;
	}

}
