package edu.byu.nlp.math;

import org.apache.commons.math3.linear.RealMatrixPreservingVisitor;

public abstract class AbstractRealMatrixPreservingVisitor implements RealMatrixPreservingVisitor {

	@Override
	public void start(int rows, int columns, int startRow, int endRow,
			int startColumn, int endColumn) {
		// do nothing
	}

	@Override
	public double end() {
		return 0;
	}
}
