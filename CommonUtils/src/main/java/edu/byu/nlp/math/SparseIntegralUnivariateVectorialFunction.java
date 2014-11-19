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
 * A univariate function whose input is an integer and whose output is a sparse vector.
 * 
 * @author rah67
 *
 */
public interface SparseIntegralUnivariateVectorialFunction {
	/**
	 * Visit the non-default values of the function at parameter x.
	 */
	void walkValuesSparsely(int x, ValueVisitor visitor);
}
