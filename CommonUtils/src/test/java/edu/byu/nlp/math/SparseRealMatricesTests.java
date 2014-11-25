package edu.byu.nlp.math;

import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.SparseRealMatrix;
import org.fest.assertions.Assertions;
import org.junit.Before;
import org.junit.Test;

import edu.byu.nlp.MoreAsserts;

public class SparseRealMatricesTests {

	private SparseRealMatrix matrix = null;
	
	@Before
	public void matrix(){
		matrix = new OpenMapRealMatrix(5, 4);
		matrix.setEntry(0, 0, 4.1);
		matrix.setEntry(0, 3, 2.1);
		matrix.setEntry(1, 1, 4.1);
		matrix.setEntry(1, 2, 4.1);
		matrix.setEntry(4, 1, 1.1);
		matrix.setEntry(4, 2, 2.1);
		matrix.setEntry(4, 3, 3.1);
	}
	
	@Test
	public void testClear(){
		SparseRealMatrix empty = new OpenMapRealMatrix(matrix.getRowDimension(), matrix.getColumnDimension());
		
		SparseRealMatrices.clear(matrix);
		MoreAsserts.assertSparseMatricesEqual(matrix, empty);
	}

	@Test
	public void testGetEntry(){
		Assertions.assertThat(matrix.getEntry(0, 0)).isEqualTo(4.1);
		Assertions.assertThat(matrix.getEntry(0, 3)).isEqualTo(2.1);
		Assertions.assertThat(matrix.getEntry(1, 1)).isEqualTo(4.1);
		Assertions.assertThat(matrix.getEntry(1, 2)).isEqualTo(4.1);
		Assertions.assertThat(matrix.getEntry(4, 1)).isEqualTo(1.1);
		Assertions.assertThat(matrix.getEntry(4, 2)).isEqualTo(2.1);
		Assertions.assertThat(matrix.getEntry(4, 3)).isEqualTo(3.1);

		Assertions.assertThat(matrix.getEntry(0, 1)).isEqualTo(0);
		Assertions.assertThat(matrix.getEntry(4, 0)).isEqualTo(0);
	}
	
	@Test
	public void testIncrement(){
		SparseRealMatrices.incrementValueAt(matrix, 0, 3, 4);
		Assertions.assertThat(matrix.getEntry(0, 3)).isEqualTo(6.1);
	}

	@Test
	public void testSum(){
		Assertions.assertThat(SparseRealMatrices.sum(matrix)).isEqualTo(20.7);
	}

	@Test
	public void testCopy(){
		SparseRealMatrix clone = new OpenMapRealMatrix(matrix.getRowDimension(), matrix.getColumnDimension());
		SparseRealMatrices.copyOnto(matrix, clone);

		Assertions.assertThat(clone.getEntry(0, 0)).isEqualTo(4.1);
		Assertions.assertThat(clone.getEntry(0, 3)).isEqualTo(2.1);
		Assertions.assertThat(clone.getEntry(1, 1)).isEqualTo(4.1);
		Assertions.assertThat(clone.getEntry(1, 2)).isEqualTo(4.1);
		Assertions.assertThat(clone.getEntry(4, 1)).isEqualTo(1.1);
		Assertions.assertThat(clone.getEntry(4, 2)).isEqualTo(2.1);
		Assertions.assertThat(clone.getEntry(4, 3)).isEqualTo(3.1);

		Assertions.assertThat(clone.getEntry(0, 1)).isEqualTo(0);
		Assertions.assertThat(clone.getEntry(4, 0)).isEqualTo(0);
	}
}
