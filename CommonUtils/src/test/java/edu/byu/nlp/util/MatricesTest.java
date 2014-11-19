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
package edu.byu.nlp.util;

import static edu.byu.nlp.test.MoreAsserts.assertMatrixEquals;
import static java.lang.Math.log;
import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotSame;

import org.fest.assertions.Delta;
import org.junit.Test;

/**
 * @author rah67
 *
 */
public class MatricesTest {

	/**
	 * Test method for {@link edu.byu.nlp.util.Matrices#clone(double[][])}.
	 */
	@Test
	public void testCloneDoubleArrayArray() {
		double[][] matrix = new double[][] { {1.0, 2.0, 3.0}, {4.0, 5.0, 6.0}, {7.0, 8.0, 9.0} };
		double[][] clone = Matrices.clone(matrix);
		
		assertNotSame(matrix, clone);
		for (int i = 0; i < matrix.length; i++) {
			assertNotSame(matrix[i], clone[i]);
			assertArrayEquals(matrix[i], clone[i], 1e-10);
		}
	}

	@Test
	public void testAddToSelfMatrix(){
    double[][] matrix = new double[][] { {1.0, 2.0, 3.0}, {4.0, 5.0, 6.0}, {7.0, 8.0, 9.0} };
    
    Matrices.addToSelf(matrix, Matrices.clone(matrix));
    
    assertMatrixEquals(matrix, 
        new double[][]{
        {2,4,6},
        {8,10,12},
        {14,16,18},
        }, 
        1e-20);
	}
	
	/**
	 * Test method for {@link edu.byu.nlp.util.Matrices#copyInto(double[][], double[][])}.
	 */
	@Test
	public void testCopyInto() {
		double[][] src = new double[][] { {1.0, 2.0, 3.0}, {4.0, 5.0, 6.0}, {7.0, 8.0, 9.0} };
		double[][] dest = new double[src.length][src[0].length];

		Matrices.copyInto(src, dest);
		
		assertNotSame(src, dest);
		for (int i = 0; i < src.length; i++) {
			assertNotSame(src[i], dest[i]);
			assertArrayEquals(src[i], dest[i], 1e-10);
		}
		
	}

	@Test
    public void testRowArgMaxInColumnMajorMatrix() throws Exception {
        double[] mat = new double[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 9, 8, 7, 6, 5, 4, 3, 2, 1 };
        assertThat(Matrices.rowArgMaxInColumnMajorMatrix(mat, 3, 0)).isEqualTo(9);
        assertThat(Matrices.rowArgMaxInColumnMajorMatrix(mat, 3, 1)).isEqualTo(7);
        assertThat(Matrices.rowArgMaxInColumnMajorMatrix(mat, 3, 2)).isEqualTo(8);
    }
	
	@Test
    public void testLogSumRowInColumnMajorMatrix() throws Exception {
        double[] mat = new double[] { log(0.8), log(0.2), log(0.1),
                                      log(0.1), log(0.7), log(0.3),
                                      log(0.1), log(0.1), log(0.6) };
        assertThat(Matrices.logSumRowInColumnMajorMatrix(mat, 3, 0)).isEqualTo(0.0, Delta.delta(1e-14));
        assertThat(Matrices.logSumRowInColumnMajorMatrix(mat, 3, 1)).isEqualTo(0.0, Delta.delta(1e-14));
        assertThat(Matrices.logSumRowInColumnMajorMatrix(mat, 3, 2)).isEqualTo(0.0, Delta.delta(1e-14));
	}
	
	@Test
	public void testGreedyRowReorderingForStrongDiagonal(){
	  double[][] mat = new double[][] {
	      {0,1,0},
	      {0,0,1},
	      {1,0,0},
	  };
	  int[] rowOrdering = Matrices.getGreedyRowReorderingForStrongDiagonal(mat);
	  assertArrayEquals(new int[]{1,2,0}, rowOrdering);
	  assertThat(Matrices.isValidMap(rowOrdering));
	  Matrices.reorderRowsToSelf(rowOrdering,mat);
	  assertMatrixEquals(mat, 
	      new double[][]{
	      {1,0,0},
	      {0,1,0},
	      {0,0,1},
	      }, 
	      1e-20);
	  
	   mat = new double[][] {
	        {.1,2,.2},
	        {.2,.1,1},
	        {1,1000,.2},
	    };
	    rowOrdering = Matrices.getGreedyRowReorderingForStrongDiagonal(mat);
	    assertArrayEquals(new int[]{2,0,1}, rowOrdering);
	    assertThat(Matrices.isValidMap(rowOrdering));
	    Matrices.reorderRowsToSelf(rowOrdering,mat);
	    assertMatrixEquals(mat, 
	        new double[][]{
	        {.2,.1,1},
	        {1,1000,.2},
	        {.1,2,.2},
	        }, 
	        1e-20);
	}

  @Test
  public void testKLDivergenceRowReorderingForStrongDiagonal(){
    double[][] mat = new double[][] {
        {0,1,0},
        {0,0,1},
        {1,0,0},
    };
    int[] rowOrdering = Matrices.getNormalizedRowReorderingForStrongDiagonal(mat);
    assertArrayEquals(new int[]{1,2,0}, rowOrdering);
    assertThat(Matrices.isValidMap(rowOrdering));
    Matrices.reorderRowsToSelf(rowOrdering,mat);
    assertMatrixEquals(mat, 
        new double[][]{
        {1,0,0},
        {0,1,0},
        {0,0,1},
        }, 
        1e-20);
    
     mat = new double[][] {
          {.1,2,.2},
          {.2,.1,1},
          {1,1000,.2},
      };
      rowOrdering = Matrices.getNormalizedRowReorderingForStrongDiagonal(mat);
      assertArrayEquals(new int[]{0,2,1}, rowOrdering);
      assertThat(Matrices.isValidMap(rowOrdering));
      Matrices.reorderRowsToSelf(rowOrdering,mat);
      assertMatrixEquals(mat, 
          new double[][]{
		  {.1,2,.2},
          {1,1000,.2},
          {.2,.1,1},
          }, 
          1e-20);
  }

  @Test
  public void testRowReorderingForStrongDiagonal(){
    double[][] mat = new double[][] {
        {0,1,0},
        {0,0,1},
        {1,0,0},
    };
    int[] rowOrdering = Matrices.getRowReorderingForStrongDiagonal(mat);
    assertArrayEquals(new int[]{1,2,0}, rowOrdering);
    assertThat(Matrices.isValidMap(rowOrdering));
    Matrices.reorderRowsToSelf(rowOrdering,mat);
    assertMatrixEquals(mat, 
        new double[][]{
        {1,0,0},
        {0,1,0},
        {0,0,1},
        }, 
        1e-20);
    
     mat = new double[][] {
          {.1,2,.2},
          {.2,.1,1},
          {1,1000,.2},
      };
      rowOrdering = Matrices.getRowReorderingForStrongDiagonal(mat);
      assertArrayEquals(new int[]{0,2,1}, rowOrdering);
      assertThat(Matrices.isValidMap(rowOrdering));
      Matrices.reorderRowsToSelf(rowOrdering,mat);
      assertMatrixEquals(mat, 
          new double[][]{
          {.1,2,.2},
          {1,1000,.2},
          {.2,.1,1},
          }, 
          1e-20);
  }
  
  @Test
  public void testGreedyColReorderingForStrongDiagonal(){
    double[][] mat = new double[][] {
        {0,1,0},
        {0,0,1},
        {1,0,0},
    };
    int[] colOrdering = Matrices.getGreedyColReorderingForStrongDiagonal(mat);
    assertArrayEquals(new int[]{2,0,1}, colOrdering);
    assertThat(Matrices.isValidMap(colOrdering));
    Matrices.reorderColsToSelf(colOrdering,mat);
    assertMatrixEquals(mat, 
        new double[][]{
        {1,0,0},
        {0,1,0},
        {0,0,1},
        }, 
        1e-20);
  }

  @Test
  public void testColReorderingForStrongDiagonal2(){
    double[][] mat = new double[][] {
        {0,0,1,0},
        {0,0,0,1},
        {0,1,0,0},
        {0,1,0,0},
    };
    int[] colOrdering = Matrices.getGreedyColReorderingForStrongDiagonal(mat);
    assertArrayEquals(new int[]{3,2,0,1}, colOrdering);
    assertThat(Matrices.isValidMap(colOrdering));
    Matrices.reorderColsToSelf(colOrdering,mat);
    assertMatrixEquals(mat, 
        new double[][]{
        {1,0,0,0},
        {0,1,0,0},
        {0,0,1,0},
        {0,0,1,0},
        }, 
        1e-20);
  }

//  @Test
//  public void testReorderingForStrongDiagonal(){
//    double[][] mat = new double[][] {
//        {0,0,1,0},
//        {1,0,0,0},
//        {0,0,1,0},
//        {0,1,0,0},
//    };
//    int[] map = new int[4];
//    if (Matrices.getReorderingForStrongDiagonal(mat, map)){
//      Matrices.reorderRowsToSelf(map,mat);
//    }
//    else{
//      Matrices.reorderColsToSelf(map,mat);
//    }
////    assertArrayEquals(new int[]{3,2,0,1}, colOrdering);
////    assertThat(Matrices.isValidMap(colOrdering));
////    Matrices.reorderColsToSelf(colOrdering,mat);
//    assertMatrixEquals(mat, 
//        new double[][]{
//        {1,0,0,0},
//        {0,1,0,0},
//        {1,0,0,0},
//        {0,0,0,1},
//        }, 
//        1e-20);
//  }

  @Test
  public void testRowReorderingbyMaxEntryDesc(){
    double[][] mat = new double[][] {
        {2,    8, 9, 0},
        {5,   33, 2, 1},
        {0.1, 11, 3, 0},
        {3,   10, 8, 0},
    };
    int[] rowOrdering = Matrices.getRowReorderingByMaxEntryDesc(mat);
    assertArrayEquals(new int[]{3,0,1,2}, rowOrdering);
    assertThat(Matrices.isValidMap(rowOrdering));
  }
  
  @Test
  public void testColReorderingbyMaxEntryDesc(){
    double[][] mat = new double[][] {
        {2,    8, 9, 0},
        {5,   33, 2, 1},
        {0.1, 11, 3, 0},
        {3,   10, 8, 0},
    };
    int[] colOrdering = Matrices.getColReorderingByMaxEntryDesc(mat);
    assertArrayEquals(new int[]{2,0,1,3}, colOrdering);
    assertThat(Matrices.isValidMap(colOrdering));
  }
  
  @Test
  public void testNormalizeRows(){
    double[][] mat = new double[][] {
        {1,2,3},
        {4,5,6},
    };
    Matrices.normalizeRowsToSelf(mat);
    assertMatrixEquals(mat, 
        new double[][]{
        {1/6.,2/6.,3/6.},
        {4/15.,5/15.,6/15.},
        }, 
        1e-20);
  }
  
  @Test
  public void testToString(){
    double[][] mat = new double[][] {
        {1.21,298.234512,3.9872},
        {41.2222,5,6.99999999},
    };
    assertThat(Matrices.toString(mat, 100, 100, 1)).isEqualTo("[[ 1.2, 298.2, 4.0]\n [41.2,   5.0, 7.0]]");

    mat = new double[][] {
        {1210},
        {42000000},
    };
    System.out.println(Matrices.toString(mat, 100, 100, 1));
    assertThat(Matrices.toString(mat, 100, 100, 1)).isEqualTo("[[    1210.0]\n [42000000.0]]");
  }
}
