package edu.byu.nlp.data.types;

import org.apache.commons.math3.linear.SparseRealVector;

import edu.byu.nlp.dataset.SparseFeatureVectors.ValueFunction;

public interface SparseFeatureVector {

	public static interface Entry {
		int getIndex();

		double getValue();
	}

	public static interface IndexVisitor {
		void visitIndex(int index);
	}

	public static interface EntryVisitor {
		void visitEntry(int index, double value);
	}
	
	double dotProduct(double[] v);

	SparseFeatureVector copy();

	void visitIndices(IndexVisitor v);

	void visitSparseEntries(EntryVisitor v);

	Iterable<Entry> sparseEntries();

	String toString();

	double sum();

	/**
	 * Computes A + v^T x this and stores the result in A.
	 */
	void preMultiplyAndAddTo(double[] v, double[][] A);

	/**
	 * Computes A + v^T x this and stores the result in A, where A is a
	 * column-major matrix starting at offset off.
	 */
	void preMultiplyAndAddTo(double[] v, double[] A, int offset);

	/**
	 * Computes v^T + A x this^T and stores the results in v
	 */
	void preMultiplyAsColumnAndAddTo(double[][] A, double[] v);

	/**
	 * Computes v^T + A x this^T and stores the results in v, where A is a
	 * column-major matrix with numColumns, starting at offset off.
	 */
	void preMultiplyAsColumnAndAddTo(double[] A, double[] v);

	/**
	 * Computes this + v and stores the result in v (vector addition)
	 */
	void addTo(double[] v);

	/**
	 * Like addTo but first multiplies this feature vector by scale
	 */
	void scaleAndAddTo(double[] v, double scale);

	/**
	 * Adds this pre-multiplied vector to the specified row of A, where A is a
	 * column-major matrix with the specified number of rows.
	 */
	void scaleAndAddToRow(double[] A, int row, int numRows,
			double scale);

	/**
	 * Adds this to the specified row of A, where A is a column-major matrix
	 * with the specified number of rows.
	 */
	void addToRow(double[] A, int row, int numRows);

	void subtractFrom(double[] v);

	void scaleAndSubtractFrom(double[] v, double scale);

	double getNumActiveFeatures();

	SparseRealVector asApacheSparseRealVector();

	/**
	 * Returns the length of the vector (the highest index number) 
	 */
	int length();
	
	void transformValues(ValueFunction f);
	
}