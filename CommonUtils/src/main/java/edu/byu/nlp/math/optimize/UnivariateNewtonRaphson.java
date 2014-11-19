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

import java.util.logging.Logger;

import org.apache.commons.math3.analysis.DifferentiableUnivariateFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.optimization.ConvergenceChecker;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.univariate.BaseUnivariateOptimizer;
import org.apache.commons.math3.optimization.univariate.UnivariatePointValuePair;

import com.google.common.base.Preconditions;

/**
 * @author rah67
 *
 */
public class UnivariateNewtonRaphson implements BaseUnivariateOptimizer<DifferentiableUnivariateFunction> {

	private static final Logger logger = Logger.getLogger(UnivariateNewtonRaphson.class.getName());
	
	private final ConvergenceChecker<UnivariatePointValuePair> convergenceChecker;
	private final int maxEvaluations;
	private int evaluations;
	
	public UnivariateNewtonRaphson(ConvergenceChecker<UnivariatePointValuePair> convergenceChecker,
			int maxEvaluations) {
		this.convergenceChecker = convergenceChecker;
		this.maxEvaluations = maxEvaluations;
		this.evaluations = 0;
	}
	
	private static class Updater {
		
		private final DifferentiableUnivariateFunction f;
		private final DifferentiableUnivariateFunction firstDerivative;
		private final UnivariateFunction secondDerivative;
		
		public static Updater newUpdater(DifferentiableUnivariateFunction f) {
			if (!(f.derivative() instanceof DifferentiableUnivariateFunction)) {
				throw new IllegalArgumentException("Gradient must be differentiable!");
			}
			
			DifferentiableUnivariateFunction firstDerivative = (DifferentiableUnivariateFunction) f.derivative();
			UnivariateFunction secondDerivative = firstDerivative.derivative();
			return new Updater(f, firstDerivative, secondDerivative);
		}
		
		private Updater(DifferentiableUnivariateFunction f, DifferentiableUnivariateFunction firstDerivative,
				UnivariateFunction secondDerivative) {
			this.f = f;
			this.firstDerivative = firstDerivative;
			this.secondDerivative = secondDerivative;
		}

		// X is changed in place; PointValuePair contains a reference to x
		private UnivariatePointValuePair update(double x) {
			// x^new = x^old - f'(x^old)/f"(x^old)
			x -= firstDerivative.value(x) / secondDerivative.value(x);
			return new UnivariatePointValuePair(x, f.value(x));
		}

	}

	/** {@inheritDoc} */
	@Override
	public ConvergenceChecker<UnivariatePointValuePair> getConvergenceChecker() {
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

	/** {@inheritDoc} */
	@Override
	public UnivariatePointValuePair optimize(int maxEval, DifferentiableUnivariateFunction f, GoalType goalType,
			double initial, double arg4) {
		Preconditions.checkArgument(maxEval > 0);
		Preconditions.checkNotNull(f);
		Preconditions.checkNotNull(goalType);
		Preconditions.checkNotNull(initial);
		
		// FIXME : goalType is being ignored!
		
		Updater updater = Updater.newUpdater(f);
		
		UnivariatePointValuePair prev = null;
		UnivariatePointValuePair next = updater.update(initial);
		logger.info(String.format("Iteration 0, value = %f", next.getValue()));

		evaluations = 1;
		do {
			prev = next;
			double x = prev.getPoint();
			next = updater.update(x);
			++evaluations;
			logger.info(String.format("Iteration %d, value = %f", evaluations, next.getValue()));
		} while (!convergenceChecker.converged(evaluations, prev, next) && evaluations < maxEval 
				&& evaluations < maxEvaluations);
		
		return next;
	}

	/** {@inheritDoc} */
	@Override
	public UnivariatePointValuePair optimize(int maxIterations, DifferentiableUnivariateFunction f, GoalType goalType,
			double initial, double arg4, double arg5) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
