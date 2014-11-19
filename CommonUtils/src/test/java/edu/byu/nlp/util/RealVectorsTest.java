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
package edu.byu.nlp.util;

import static java.lang.Math.log;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.junit.Test;

import edu.byu.nlp.math.RealVectors;

/**
 * Unit tests for RealVectors.
 * 
 * @author rah67
 *
 */
public class RealVectorsTest {

	/**
	 * Test method for {@link edu.byu.nlp.math.RealVectors#logSumSloppy(RealVector)}.
	 */
	@Test
	public void testLogSum() {
		// NPE
		try {
			RealVectors.logSumSloppy(null);
			fail("Expecting null pointer exception");
		} catch (Exception expected) {}
		
		// Normal usage
		assertEquals(log(15),
				RealVectors.logSumSloppy(new ArrayRealVector(new double[]{log(1), log(2), log(3), log(4), log(5)})),
				1e-14);
		
		// Skip elements that are 20 of magnitues smaller
		assertEquals(log(10.0),
				RealVectors.logSumSloppy(new ArrayRealVector(new double[]{log(1e-20), log(1), log(2), log(3), log(4)})),
				1e-14);
	}
}
