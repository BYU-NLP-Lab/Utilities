package edu.byu.nlp.math;

import org.apache.commons.math3.linear.RealMatrixPreservingVisitor;
import org.apache.commons.math3.linear.SparseRealMatrix;

public class SparseRealMatrices {

	private SparseRealMatrices(){}
	
	public static double sum(SparseRealMatrix matrix){
		if (matrix==null){
			return 0;
		}
		return matrix.walkInOptimizedOrder(new RealMatrixPreservingVisitor() {
			double total = 0;
			@Override
			public void visit(int row, int column, double value) {
				total += value;
			}
			@Override
			public void start(int rows, int columns, int startRow, int endRow, int startColumn, int endColumn) {
			}
			@Override
			public double end() {
				return total;
			}
		});
	}
	
}
