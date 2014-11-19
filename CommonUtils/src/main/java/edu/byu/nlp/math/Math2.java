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
package edu.byu.nlp.math;

import com.google.common.base.Preconditions;

/**
 * @author rah67
 *
 */
public class Math2 {

	public static final int LOGSUM_THRESHOLD = -50;

	private Math2() { }

	public static boolean doubleEquals(double a, double b, double tolerance) {
		return Math.abs(a - b) < tolerance;
	}

	public static boolean isIntegral(double a, double tolerance) {
		return doubleEquals(a, Math.round(a), tolerance);
	}
	
	public static double logAddSloppy(double x, double y) {
		if (x < y) {
			double tmp = x;
			x = y;
			y = tmp;
		}
		
		if (x == Double.NEGATIVE_INFINITY) {
			return x;
		}
		
		double negDiff = y - x;
		if (negDiff < LOGSUM_THRESHOLD) {
			return x;
		}
		
		return x + Math.log(1.0 + Math.exp(negDiff));
	}
	
	public static double logSubtractSloppy(double x, double y) {
		if (x == Double.NEGATIVE_INFINITY) {
			return x;
		}

		Preconditions.checkArgument(x >= y, "Cannot take the log of a negative difference");
		
		double negDiff = y - x;
		if (negDiff < LOGSUM_THRESHOLD) {
			return x;
		}

		return x + Math.log(1.0 - Math.exp(negDiff));
	}
}
