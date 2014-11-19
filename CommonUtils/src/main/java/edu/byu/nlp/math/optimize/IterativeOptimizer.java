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

/**
 * @author rah67
 *
 */
public class IterativeOptimizer {

	static final Logger logger = Logger.getLogger(IterativeOptimizer.class.getName());
	
	public static interface Optimizable<T> {
		// TODO : add facilities to lazily compute the value so that we can skip computation
		// when not reporting the value.
		ValueAndObject<T> computeNext(T curObject);
	}
	
	public static enum ReturnType { HIGHEST, LOWEST, LAST }

	private final ConvergenceChecker convergenceChecker;
	
	public IterativeOptimizer(ConvergenceChecker convergenceChecker) {
		this.convergenceChecker = convergenceChecker;
	}
	
	public <T> ValueAndObject<T> optimize(Optimizable<T> optimizable, ReturnType returnType, boolean strictlyIncreasing,
			T initialObject) {
		
		double prevVal;
		ValueAndObject<T> curValueAndObject = optimizable.computeNext(initialObject);
		ValueAndObject<T> toKeep = curValueAndObject;
		logger.info(String.format("Iteration 0, value = %f", curValueAndObject.getValue()));
		
		int it = 1;
		do {
			// FIXME: add hooks for mutable objects (e.g. (Un)collapsedParameters)
			prevVal = curValueAndObject.getValue();
			curValueAndObject = optimizable.computeNext(curValueAndObject.getObject());
			if (returnType == ReturnType.LAST ||
					returnType == ReturnType.HIGHEST && curValueAndObject.getValue() > toKeep.getValue() ||
					returnType == ReturnType.LOWEST && curValueAndObject.getValue() < toKeep.getValue()) {
				toKeep = curValueAndObject;
			}
			logger.info(String.format("Iteration %d, value = %f, keeping = %f", it, curValueAndObject.getValue(),
					toKeep.getValue()));

			// Ensure that the value is strictly non-decreasing, if necessary
			if (strictlyIncreasing && prevVal > curValueAndObject.getValue()) {
				logger.warning(String.format("On iteration %d, the value decreased from %f to %f",
						it, prevVal, curValueAndObject.getValue()));
			}
		} while (!convergenceChecker.isConverged(it++, prevVal, curValueAndObject.getValue()));
		
		return toKeep;
	}
	
}
