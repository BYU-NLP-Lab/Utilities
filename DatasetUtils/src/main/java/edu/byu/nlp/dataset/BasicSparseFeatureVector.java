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
package edu.byu.nlp.dataset;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math3.linear.OpenMapRealVector;
import org.apache.commons.math3.linear.SparseRealVector;

import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Lists;

import edu.byu.nlp.data.types.SparseFeatureVector;
import edu.byu.nlp.dataset.SparseFeatureVectors.ValueFunction;
import edu.byu.nlp.util.DoubleArrays;
import edu.byu.nlp.util.IntArrays;

/**
 * @author rah67 
 * 
 * Note(pfelt): Conceptually, this class is redundant with
 *         apache's SparseRealVector class. However, we've chosen to
 *         re-implement the needed functionalities by hand because we 
 *         often update large vectors and matrices in place. Apache's
 *         operations generally make copies. So we end up re-implementing 
 *         all of these operations anyways. 
 */
public class BasicSparseFeatureVector implements SparseFeatureVector {

	public static class BasicEntry implements Entry {
		private final int index;
		private final double value;

		public BasicEntry(int index, double value) {
			this.index = index;
			this.value = value;
		}

		@Override
		public int getIndex() {
			return index;
		}

		@Override
		public double getValue() {
			return value;
		}

		/** {@inheritDoc} */
		@Override
		public String toString() {
			return "BasicEntry [index=" + index + ", value=" + value + "]";
		}

		/** {@inheritDoc} */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + index;
			long temp;
			temp = Double.doubleToLongBits(value);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			return result;
		}

		/** {@inheritDoc} */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			BasicEntry other = (BasicEntry) obj;
			if (index != other.index)
				return false;
			if (Double.doubleToLongBits(value) != Double
					.doubleToLongBits(other.value))
				return false;
			return true;
		}
	}

	private final int[] indices;
	private final double[] values;

	public static BasicSparseFeatureVector fromDenseFeatureVector(double[] denseVector){
		List<Integer> indices = Lists.newArrayList();
		List<Double> values = Lists.newArrayList();
		for (int i=0; i<denseVector.length; i++){
			if (denseVector[i]!=0){
				indices.add(i);
				values.add(denseVector[i]);
			}
		}
		// preserve length info by adding the extreme index with value 0 (if necessary)
		if (!indices.contains(denseVector.length-1)){
			indices.add(denseVector.length-1);
			values.add(0.0);
		}
		return new BasicSparseFeatureVector(IntArrays.fromList(indices), DoubleArrays.fromList(values));
	}
	
	public BasicSparseFeatureVector(int[] indices, double[] values) {
		Preconditions.checkNotNull(indices);
		Preconditions.checkNotNull(values);
		Preconditions.checkArgument(indices.length==values.length);
		this.indices = indices;
		this.values = values;
	}

	/* (non-Javadoc)
	 * @see edu.byu.nlp.data.Temp#dotProduct(double[])
	 */
	@Override
	public double dotProduct(double[] v) {
		double dotProduct = 0.0;
		for (int i = 0; i < values.length; i++) {
			dotProduct += values[i] * v[indices[i]];
		}
		return dotProduct;
	}

	/* (non-Javadoc)
	 * @see edu.byu.nlp.data.Temp#copy()
	 */
	@Override
	public BasicSparseFeatureVector copy() {
		return new BasicSparseFeatureVector(indices.clone(), values.clone());
	}

	@Override
	public void transformValues(ValueFunction f) {
		for (int i = 0; i < values.length; i++) {
			values[i] = f.apply(indices[i], values[i]);
		}
	}

	/* (non-Javadoc)
	 * @see edu.byu.nlp.data.Temp#visitIndices(edu.byu.nlp.data.BasicSparseFeatureVector.IndexVisitor)
	 */
	@Override
	public void visitIndices(IndexVisitor v) {
		for (int index : indices) {
			v.visitIndex(index);
		}
	}

	/* (non-Javadoc)
	 * @see edu.byu.nlp.data.Temp#visitSparseEntries(edu.byu.nlp.data.BasicSparseFeatureVector.EntryVisitor)
	 */
	@Override
	public void visitSparseEntries(EntryVisitor v) {
		for (int i = 0; i < indices.length; i++) {
			v.visitEntry(indices[i], values[i]);
		}
	}

	private final class SparseEntryIterator extends AbstractIterator<Entry> {

		private int i = 0;

		@Override
		protected Entry computeNext() {
			if (i >= indices.length) {
				return super.endOfData();
			}
			Entry e = new BasicEntry(indices[i], values[i]);
			++i;
			return e;
		}
	}

	/* (non-Javadoc)
	 * @see edu.byu.nlp.data.Temp#sparseEntries()
	 */
	@Override
	public Iterable<Entry> sparseEntries() {
		return new Iterable<Entry>() {

			@Override
			public Iterator<Entry> iterator() {
				return new SparseEntryIterator();
			}

		};
	}

	/* (non-Javadoc)
	 * @see edu.byu.nlp.data.Temp#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[ ");
		for (int i = 0; i < indices.length; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(indices[i]);
			sb.append(':');
			sb.append(values[i]);
		}
		sb.append(" ]");
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see edu.byu.nlp.data.Temp#sum()
	 */
	@Override
	public double sum() {
		double sum = 0.0;
		for (double v : values) {
			sum += v;
		}
		return sum;
	}

	/* (non-Javadoc)
	 * @see edu.byu.nlp.data.Temp#preMultiplyAndAddTo(double[], double[][])
	 */
	@Override
	public void preMultiplyAndAddTo(double[] v, double[][] A) {
		for (int i = 0; i < indices.length; i++) {
			int column = indices[i];
			double value = values[i];
			for (int row = 0; row < v.length; row++) {
				A[row][column] += v[row] * value;
			}
		}
	}

	/* (non-Javadoc)
	 * @see edu.byu.nlp.data.Temp#preMultiplyAndAddTo(double[], double[], int)
	 */
	@Override
	public void preMultiplyAndAddTo(double[] v, double[] A, int offset) {
		for (int i = 0; i < indices.length; i++) {
			int column = indices[i];
			double value = values[i];
			int index = offset + v.length * column;
			for (int row = 0; row < v.length; row++) {
				A[index++] += v[row] * value;
			}
		}
	}

	/* (non-Javadoc)
	 * @see edu.byu.nlp.data.Temp#preMultiplyAsColumnAndAddTo(double[][], double[])
	 */
	@Override
	public void preMultiplyAsColumnAndAddTo(double[][] A, double[] v) {
		for (int i = 0; i < indices.length; i++) {
			int index = indices[i];
			double value = values[i];
			for (int row = 0; row < A.length; row++) {
				v[row] += A[row][index] * value;
			}
		}
	}

	/* (non-Javadoc)
	 * @see edu.byu.nlp.data.Temp#preMultiplyAsColumnAndAddTo(double[], double[])
	 */
	@Override
	public void preMultiplyAsColumnAndAddTo(double[] A, double[] v) {
		for (int i = 0; i < indices.length; i++) {
			int column = indices[i];
			double value = values[i];
			int index = v.length * column;
			for (int row = 0; row < v.length; row++) {
				v[row] += A[index++] * value;
			}
		}
	}

	/* (non-Javadoc)
	 * @see edu.byu.nlp.data.Temp#addTo(double[])
	 */
	@Override
	public void addTo(double[] v) {
		for (int i = 0; i < indices.length; i++) {
			v[indices[i]] += values[i];
		}
	}

	/* (non-Javadoc)
	 * @see edu.byu.nlp.data.Temp#scaleAndAddTo(double[], double)
	 */
	@Override
	public void scaleAndAddTo(double[] v, double scale) {
		for (int i = 0; i < indices.length; i++) {
			v[indices[i]] += values[i] * scale;
		}
	}

	/* (non-Javadoc)
	 * @see edu.byu.nlp.data.Temp#scaleAndAddToRow(double[], int, int, double)
	 */
	@Override
	public void scaleAndAddToRow(double[] A, int row, int numRows, double scale) {
		for (int i = 0; i < indices.length; i++) {
			A[numRows * indices[i] + row] += values[i] * scale;
		}
	}

	/* (non-Javadoc)
	 * @see edu.byu.nlp.data.Temp#addToRow(double[], int, int)
	 */
	@Override
	public void addToRow(double[] A, int row, int numRows) {
		for (int i = 0; i < indices.length; i++) {
			A[numRows * indices[i] + row] += values[i];
		}
	}

	/* (non-Javadoc)
	 * @see edu.byu.nlp.data.Temp#subtractFrom(double[])
	 */
	@Override
	public void subtractFrom(double[] v) {
		for (int i = 0; i < indices.length; i++) {
			v[indices[i]] -= values[i];
		}
	}

	/* (non-Javadoc)
	 * @see edu.byu.nlp.data.Temp#scaleAndSubtractFrom(double[], double)
	 */
	@Override
	public void scaleAndSubtractFrom(double[] v, double scale) {
		for (int i = 0; i < indices.length; i++) {
			v[indices[i]] -= values[i] * scale;
		}
	}

	/* (non-Javadoc)
	 * @see edu.byu.nlp.data.Temp#getNumActiveFeatures()
	 */
	@Override
	public double getNumActiveFeatures() {
		return indices.length;
	}

	/* (non-Javadoc)
	 * @see edu.byu.nlp.data.Temp#asApacheSparseRealVector()
	 */
	@Override
	public SparseRealVector asApacheSparseRealVector() {
		SparseRealVector retval = new OpenMapRealVector();
		for (int i = 0; i < indices.length; i++) {
			retval.setEntry(indices[i], values[i]);
		}
		return retval;
	}
	
	/* (non-Javadoc)
	 * @see edu.byu.nlp.data.Temp#length()
	 */
	@Override
	public int length(){
//		// TODO: ensure indices are sorted and then do:
//		return indices[indices.length-1]+1;
		return IntArrays.max(indices)+1;
	}

	public static Comparator<Entry> valueComparator() {
		return new Comparator<Entry>() {
			@Override
			public int compare(Entry entry1, Entry entry2) {
				return Double.compare(entry1.getValue(), entry2.getValue());
			}
		};
	}
}
