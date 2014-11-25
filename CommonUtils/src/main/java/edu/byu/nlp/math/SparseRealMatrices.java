package edu.byu.nlp.math;

import java.util.logging.Logger;

import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.linear.AbstractRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealMatrixChangingVisitor;
import org.apache.commons.math3.linear.RealMatrixPreservingVisitor;
import org.apache.commons.math3.linear.SparseRealMatrix;

import com.google.common.base.Preconditions;

import edu.byu.nlp.util.Counter;
import edu.byu.nlp.util.DenseCounter;
import edu.byu.nlp.util.Doubles;

public class SparseRealMatrices {

	private static final Logger logger = Logger.getLogger(SparseRealMatrices.class.getName());
	
	private SparseRealMatrices(){}
	
	public static double sum(SparseRealMatrix matrix){
		if (matrix==null){
			return 0;
		}
		if (matrix.getRowDimension()==0 || matrix.getColumnDimension()==0){
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

	public static double rowSum(int row, SparseRealMatrix matrix){
		if (matrix==null){
			return 0;
		}
		return RealVectors.sum(matrix.getRowVector(row));
	}
	
	public static Counter<Integer> countColumns(SparseRealMatrix matrix){
		final Counter<Integer> counter = new DenseCounter(matrix.getColumnDimension());
		matrix.walkInOptimizedOrder(new AbstractRealMatrixPreservingVisitor() {
			@Override
			public void visit(int row, int column, double value) {
				counter.incrementCount(column, (int)Doubles.longFrom(value, 1e-10));
			}
		});
		return counter;
	}
	
	
	private static class NullRowSparseRealMatrix extends AbstractRealMatrix implements SparseRealMatrix{
		private int numCols;
		public NullRowSparseRealMatrix(int numCols){
			this.numCols=numCols;
		}
		@Override
		public int getRowDimension() {
			return 0;
		}
		@Override
		public int getColumnDimension() {
			return numCols;
		}
		@Override
		public RealMatrix createMatrix(int rowDimension, int columnDimension) throws NotStrictlyPositiveException {
			throw new UnsupportedOperationException();
		}
		@Override
		public RealMatrix copy() {
			return this;
		}
		@Override
		public double getEntry(int row, int column) throws OutOfRangeException {
			throw new UnsupportedOperationException("this matrix has 0 rows");
		}
		@Override
		public void setEntry(int row, int column, double value)throws OutOfRangeException {
			throw new UnsupportedOperationException("this matrix has 0 rows");
		}
	}
	/**
	 * A represenation of a sparse matrix with 0 rows. 
	 * Not matrix operations are defined on such a matrix 
	 * other than simple things like getRowDimension and getColDimension.
	 * For most operations, throws an unsupportedOperationException.
	 */
	public static SparseRealMatrix nullRowSparseMatrix(int numCols){
		return new NullRowSparseRealMatrix(numCols);
	}

	public static void incrementValueAt(SparseRealMatrix matrix, int row, int column){
		incrementValueAt(matrix, row, column, 1);
	}
	
	public static void incrementValueAt(SparseRealMatrix matrix, int row, int column, double value){
		Preconditions.checkArgument(row<matrix.getRowDimension());
		Preconditions.checkArgument(column<matrix.getColumnDimension());
		matrix.setEntry(row, column, matrix.getEntry(row, column)+value);
	}
	
	/**
	 * Copy all values from src onto dest. If a value is out of range, ignore it.
	 */
	public static void copyOnto(SparseRealMatrix src, final SparseRealMatrix dest){
		src.walkInOptimizedOrder(new AbstractRealMatrixPreservingVisitor() {
			@Override
			public void visit(int row, int column, double value) {
				if (row<dest.getRowDimension() && column<dest.getColumnDimension()){
					dest.setEntry(row, column, value);
				}
				else{
					logger.warning("matrix entry out of bounds while copying. Losing entry ["+row+","+column+"]");
				}
			}
		});
	}

	public static void clear(SparseRealMatrix labelAnnotations) {
		labelAnnotations.walkInOptimizedOrder(new RealMatrixChangingVisitor() {
			@Override
			public double visit(int row, int column, double value) {
				// this sets each entry to 0
				return 0;
			}
			@Override
			public void start(int rows, int columns, int startRow, int endRow,
					int startColumn, int endColumn) {
			}
			@Override
			public double end() {
				return 0;
			}
		});
	}
	
}
