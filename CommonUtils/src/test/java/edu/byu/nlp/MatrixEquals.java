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
package edu.byu.nlp;

import java.util.Arrays;

import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;

/**
 * @author rah67
 *
 */
public class MatrixEquals implements IArgumentMatcher {

	private final double[][] expected;
	
	public MatrixEquals(double[][] expected) {
		this.expected = expected;
	}

	/** {@inheritDoc} */
	@Override
	public boolean matches(Object actualObj) {
		if (!(actualObj instanceof double[][])) {
			return false;
		}
		return Arrays.deepEquals(expected, (double[][]) actualObj);
	}

	/** {@inheritDoc} */
	@Override
	public void appendTo(StringBuffer sb) {
		sb.append("matrixEq(");
		sb.append(Arrays.deepToString(expected));
		sb.append(')');
	}
	
	public static double[][] matrixEq(double[][] expected) {
		EasyMock.reportMatcher(new MatrixEquals(expected));
		return null;
	}
	
	
}
