package edu.byu.nlp.dataset;

import org.apache.commons.math3.linear.OpenMapRealMatrix;

import edu.byu.nlp.data.types.SparseFeatureMatrix;
import edu.byu.nlp.data.types.SparseFeatureVector;

public class SingleRowFeatureMatrix extends OpenMapRealMatrix implements SparseFeatureMatrix {
	private static final long serialVersionUID = 1L;

	public SingleRowFeatureMatrix(SparseFeatureVector vector){
		super(1, vector.length());
		int asd = vector.length();
		// populate the first row with vector info
		final OpenMapRealMatrix thismatrix = this;
		vector.visitSparseEntries(new BasicSparseFeatureVector.EntryVisitor() {
			@Override
			public void visitEntry(int index, double value) {
				thismatrix.setEntry(0, index, value);
			}
		});
	}
	
}
