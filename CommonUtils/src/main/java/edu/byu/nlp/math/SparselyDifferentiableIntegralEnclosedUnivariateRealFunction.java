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

/**
 * A closure that encapsulates all but a single integral parameter of a differentiable function. For instance, suppose
 * we have a differentiable function f(a, b, c). Let function g be a closure of f for the variables a and b, i.e.
 * g_{a, b}(c) = f(a, b, c). g is now a univariate function (of c), but the gradient is w.r.t to a, b, and c.
 * As a result, the gradient is multivariate, even though the function itself is univariate. This interface assumes that
 * the gradient is sparse, i.e., that many of the values in the gradienty are zero.
 * 
 * @author rah67
 *
 */
public interface SparselyDifferentiableIntegralEnclosedUnivariateRealFunction extends IntegralUnivariateFunction {
	/**
	 * Returns the derivative of the function.
	 */
	SparseIntegralUnivariateVectorialFunction sparseGradient();
}