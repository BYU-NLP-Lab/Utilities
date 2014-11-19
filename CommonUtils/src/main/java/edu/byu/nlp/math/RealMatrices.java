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

import org.apache.commons.math3.linear.AbstractRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

/**
 * @author rah67
 *
 */
public class RealMatrices {

	private RealMatrices() { }
	
	private static class RepeatedRowMatrix extends AbstractRealMatrix {

		private final RealVector v;
		private final int numRows;
		
		public RepeatedRowMatrix(RealVector v, int numRows) {
			this.v = v;
			this.numRows = numRows;
		}
		
		/** {@inheritDoc} */
		@Override
		public RealMatrix copy() {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc} */
		@Override
		public RealMatrix createMatrix(int rowDimension, int colDimension) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc} */
		@Override
		public int getColumnDimension() {
			return v.getDimension();
		}

		/** {@inheritDoc} */
		@Override
		public double getEntry(int row, int col) {
			return v.getEntry(col);
		}

		/** {@inheritDoc} */
		@Override
		public int getRowDimension() {
			return numRows;
		}

		/** {@inheritDoc} */
		@Override
		public void setEntry(int arg0, int arg1, double arg2) {
			throw new UnsupportedOperationException();
		}
		
	}
	
	public static RealMatrix repeatRow(RealVector v, int numRows) {
		return new RepeatedRowMatrix(v, numRows);
	}
}
