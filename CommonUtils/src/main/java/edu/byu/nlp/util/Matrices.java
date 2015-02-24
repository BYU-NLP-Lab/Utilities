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

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.PointValuePair;
import org.apache.commons.math3.optimization.linear.LinearConstraint;
import org.apache.commons.math3.optimization.linear.LinearObjectiveFunction;
import org.apache.commons.math3.optimization.linear.Relationship;
import org.apache.commons.math3.optimization.linear.SimplexSolver;
import org.apache.commons.math3.random.RandomGenerator;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gson.Gson;

import edu.byu.nlp.math.Math2;

/**
 * @author rah67
 *
 */
public class Matrices {

	private Matrices() { }
	
	public static double mean(double[][] mat){
		Preconditions.checkNotNull(mat);
		Preconditions.checkArgument(mat.length>0);
		Preconditions.checkArgument(mat[0].length>0);
		int rows = mat.length;
		int cols = mat[0].length;
		int size = rows*cols;
		return sum(mat)/size;	
	}
	
	public static void subtractFromSelf(double[][] mat, double beta) {
		for (int i = 0; i < mat.length; i++) {
			for (int j = 0; j < mat[i].length; j++) {
				mat[i][j] -= beta;
			}
		}
	}

  public static void subtractFromSelf(double[][][] mat, double[][][] other) {
	  for (int i=0; i<mat.length; i++){
		  subtractFromSelf(mat[i], other[i]);
	  }
  }
	  
  public static void subtractFromSelf(double[][] mat, double[][] other) {
    Preconditions.checkNotNull(mat);
    Preconditions.checkNotNull(other);
    Preconditions.checkArgument(mat.length==other.length);
    for (int i = 0; i < mat.length; i++) {
      Preconditions.checkArgument(mat[i].length==other[i].length);
      for (int j = 0; j < mat[i].length; j++) {
        mat[i][j] -= other[i][j];
      }
    }
  }

	public static void addToSelf(double[][][] mat, double beta) {
		for (int i = 0; i < mat.length; i++) {
			addToSelf(mat[i], beta);
		}
	}
	
	public static void addToSelf(double[][] mat, double beta) {
		for (int i = 0; i < mat.length; i++) {
			for (int j = 0; j < mat[i].length; j++) {
				mat[i][j] += beta;
			}
		}
	}

  public static void addToSelf(double[][][] mat, double[][][] other) {
	  for (int i=0; i<mat.length; i++){
		  addToSelf(mat[i], other[i]);
	  }
  }
	  
  public static void addToSelf(double[][] mat, double[][] other) {
    Preconditions.checkNotNull(mat);
    Preconditions.checkNotNull(other);
    Preconditions.checkArgument(mat.length==other.length);
    for (int i = 0; i < mat.length; i++) {
      Preconditions.checkArgument(mat[i].length==other[i].length);
      for (int j = 0; j < mat[i].length; j++) {
        mat[i][j] += other[i][j];
      }
    }
  }

  public static double[][][] clone(double[][][] arr) {
    double[][][] clone = new double[arr.length][][];
    for (int i = 0; i < clone.length; i++) {
      clone[i] = clone(arr[i]);
    }
    return clone;
  }
  
	public static double[][] clone(double[][] arr) {
		double[][] clone = new double[arr.length][];
		for (int i = 0; i < clone.length; i++) {
			clone[i] = arr[i].clone();
		}
		return clone;
	}
	
	public static int[][] clone(int[][] arr) {
		int[][] clone = new int[arr.length][];
		for (int i = 0; i < clone.length; i++) {
			clone[i] = arr[i].clone();
		}
		return clone;
	}

	/**
	 * Computes ret[i] = \sum_j mat[j][i]
	 */
	public static double[] sumOverFirst(double[][] mat) {
		Preconditions.checkNotNull(mat);
		if (mat.length == 0) {
			return new double[0];
		}
		
		double[] sum = new double[mat[0].length];
		for (int i = 0; i < mat.length; i++) {
			Preconditions.checkArgument(mat[i].length == sum.length, "matrix must be square");
			for (int j = 0; j < mat[i].length; j++) {
				sum[j] += mat[i][j];
			}
		}
		return sum;
	}

	/**
	 * Computes ret[i] = \sum_j mat[i][j]
	 */
	public static double[] sumOverSecond(double[][] mat) {
		Preconditions.checkNotNull(mat);
		
		double[] sum = new double[mat.length];
		for (int i = 0; i < sum.length; i++) {
			sum[i] = DoubleArrays.sum(mat[i]);
		}
		return sum;
	}

  /**
   * Calls DoubleArrays.logNormalizeToSelf on each row
   */
  public static void logNormalizeRowsToSelf(double[][] mat) {
    Preconditions.checkNotNull(mat);

    for (int r=0; r<mat.length; r++){
      DoubleArrays.logNormalizeToSelf(mat[r]);
    }
  }

  public static void normalizeRowsToSelf(double[][][] tensor) {
	  Preconditions.checkNotNull(tensor);
	  for (int i=0; i<tensor.length; i++){
		  normalizeRowsToSelf(tensor[i]);
	  }
  }
  
	/**
   * Calls DoubleArrays.normalizeToSelf on each row
	 * Computes mat[i][j] = mat[i][j] / \sum_j mat[i][j]
	 */
  public static void normalizeRowsToSelf(double[][] mat) {
    Preconditions.checkNotNull(mat);
    
    for (int r=0; r<mat.length; r++){
      DoubleArrays.normalizeToSelf(mat[r]);
    }
  }
	

	public static void fill(double[][] matrix, double x) {
		for (double[] row : matrix) {
			Arrays.fill(row, x);
		}
	}

	public static void fill(double[][] matrix, double[] x) {
		Preconditions.checkArgument(matrix.length == x.length);
		for (int i = 0; i < matrix.length; i++) {
			Arrays.fill(matrix[i], x[i]);
		}
	}

	public static double sum(double[][] mat) {
		double sum = 0.0;
		for (int i = 0; i < mat.length; i++) {
			for (int j = 0; j < mat[i].length; j++) {
				sum += mat[i][j];
			}
		}
		return sum;
	}
	
	public static double trace(double[][] mat){
	  Preconditions.checkNotNull(mat);
	  if (mat.length==0) return 0;
	  Preconditions.checkArgument(mat.length==mat[0].length);
	  double trace = 0.0;
	  for (int r=0; r<mat.length; r++){
	    trace += mat[r][r];
	  }
	  return trace;
	}

	public static void divideToSelf(double[][] matrix, double x) {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				matrix[i][j] /= x;
			}
		}
	}

	public static double[][] of(double fill, int rows, int cols) {
		double[][] mat = new double[rows][];
		for (int r = 0; r < rows; r++) {
			mat[r] = DoubleArrays.constant(fill, cols);
		}
		return mat;
	}

	/**
	 * Copies the first matrix into the other.
	 */
	public static void copyInto(double[][] src, double[][] dest) {
		for (int i = 0; i < src.length; i++) {
			System.arraycopy(src[i], 0, dest[i], 0, src[i].length);
		}
	}

	// Returns the actual index (not the column number) of the element in the specified row having the
	// highest value
	public static int rowArgMaxInColumnMajorMatrix(double[] mat, int numRows, int row) {
	    int argMax = row;
	    double max = mat[argMax];
	    for (int i = row + numRows; i < mat.length; i += numRows) {
	        if (mat[i] > max) {
	            max = mat[i];
	            argMax = i;
	        }
	    }
	    assert argMax % numRows == row;
	    return argMax;
	}
	
    public static double logSumRowInColumnMajorMatrix(double[] mat, int numRows, int row) {
        double sumExponentiatedDiffs = 0.0;
        int argMax = rowArgMaxInColumnMajorMatrix(mat, numRows, row);
        double max = mat[argMax];
        
        for (int i = row; i < argMax; i += numRows) {
            double diff = mat[i] - max;
            if (diff >= Math2.LOGSUM_THRESHOLD) {
                sumExponentiatedDiffs += Math.exp(diff);
            }
        }
        for (int i = argMax + numRows; i < mat.length; i += numRows) {
            double diff = mat[i] - max;
            if (diff >= Math2.LOGSUM_THRESHOLD) {
                sumExponentiatedDiffs += Math.exp(diff);
            }
        }
        return max + Math.log(1.0 + sumExponentiatedDiffs);
    }
    
    public static double[][] log(double[][] mat) {
      double[][] m2 = clone(mat);
      logToSelf(m2);
      return m2;
    }

    public static void logToSelf(double[][][] tensor) {
      for (int i=0; i<tensor.length; i++){
        logToSelf(tensor[i]);
      }
    }
    
    public static void logToSelf(double[][] mat) {
      for (int i = 0; i < mat.length; i++) {
        for (int j = 0; j < mat[i].length; j++) {
          mat[i][j] = Math.log(mat[i][j]);
        }
      }
    }

    public static void expToSelf(double[][] mat) {
      for (int i = 0; i < mat.length; i++) {
        DoubleArrays.expToSelf(mat[i]);
      }
    }
    
    public static double[][] exp(double[][] mat) {
      double[][] copy = new double[mat.length][];
      for (int i = 0; i < copy.length; i++) {
        copy[i] = DoubleArrays.exp(mat[i]);
      }
      return copy;
    }

    public static int[] argMaxesInColumns(double[][] mat) {
      int[] argMaxes = new int[mat[0].length];
      double[] maxes = DoubleArrays.constant(Double.NEGATIVE_INFINITY, argMaxes.length);
      for (int i = 0; i < mat.length; i++) {
        for (int j = 0; j < mat[i].length; j++) {
          if (mat[i][j] > maxes[j]) {
            maxes[j] = mat[i][j];
            argMaxes[j] = i;
          }
        }
      }
      return argMaxes;
    }
    
    public static double[][] transpose(double[][] mat){
      // not the most efficient implementation, but this isn't 
      // currently being used in any inner loops
      return new Array2DRowRealMatrix(mat).transpose().getData();
    }

    @VisibleForTesting
    static boolean isValidMap(int[] map) {
      int[] sorted = map.clone();
      Arrays.sort(sorted);
      for (int i = 0; i < sorted.length; i++) {
        if (sorted[i] != i) {
          return false;
        }
      }
      return true;
    }

    public static int[] getColReorderingByMaxEntryDesc(double[][] mat) {
      return getRowReorderingByMaxEntryDesc(transpose(mat));
    }
    
    public static int[] getRowReorderingByMaxEntryDesc(double[][] mat){
      Preconditions.checkNotNull(mat);
      
      // sort indices by max entry
      List<RowMax> maxes = Lists.newArrayList();
      for (int r=0; r<mat.length; r++){
        double v = DoubleArrays.max(mat[r]);
        maxes.add(new RowMax(r,v));
      }
      Collections.sort(maxes);
      // codify the sorting
      int map[] = new int[mat.length];
      for (int r=0; r<mat.length; r++){
        map[maxes.get(r).i] = r;
      }
      return map;
    }
    private static class RowMax implements Comparable<RowMax>{
      int i = -1;
      double v = Double.NEGATIVE_INFINITY;
      public RowMax(int i, double v){this.i=i; this.v=v;}
      @Override
      public int compareTo(RowMax o) {
        return -1*Double.compare(this.v, o.v); // desc
      }
      @Override
      public String toString() {
        return "("+i+","+v+")"; 
      }
    }
    
    public static int[] getGreedyColReorderingForStrongDiagonal(double[][] mat){
      return getGreedyRowReorderingForStrongDiagonal(transpose(mat));
    }
    
    public static int[] getGreedyRowReorderingForStrongDiagonal(double[][] mat){
      Preconditions.checkNotNull(mat);
      int numRows = mat.length;
      int numCols = (mat.length==0)? 0: mat[0].length;
      
      // order rows in descending order by entry value
      // so that more confident rows get first choice
      int[] rowOrder = getRowReorderingByMaxEntryDesc(mat);
      
      int[] map = new int[numRows];
      Arrays.fill(map, -1);
      for (int i = 0; i < numRows; i++) {
        int row = IntArrays.indexOf(i, rowOrder, 0, rowOrder.length);
        
        int maxcol = -1;
        double max = Double.NEGATIVE_INFINITY;
        for (int col=0; col<numCols; col++){
          if (mat[row][col] > max){
            // Only choose this index if we haven't already used it (greedy)
            if (IntArrays.indexOf(col, map, 0, map.length) == -1){
              max = mat[row][col];
              maxcol = col;
            }
          }
        }
        // assign the original row index a new destination (to put it on the diag) 
        map[row] = maxcol;
      }
      assert isValidMap(map);
      return map;
    }
    
//    private static double klDivergenceWithStrongDiagonal(double[][]mat, double smoothingFactor){
//      Preconditions.checkArgument(0 < smoothingFactor && smoothingFactor<1,"smoothingFactor should be strictly between 0 and 1 (0 entries cause bad behavior)");
//      double total = 0.0;
//      double smoothingNormalizer = smoothingFactor*mat.length + 1;
//      double diag = (1 + smoothingFactor) / smoothingNormalizer;
//      double offDiag = smoothingFactor / smoothingNormalizer;
//      for (int r=0; r<mat.length; r++){
//        double[] row = mat[r];
//        for (int c=0; c<row.length; c++){
//          double pval = (mat[r][c]+smoothingFactor)/smoothingNormalizer;
//          double qval = (r==c)? diag: offDiag;
//          total += pval * Math.log(pval / qval);
//        }
//      }
//      return total;
//    }
    
//    /**
//     * Finds an ordering that will produce a 
//     * matrix with strong diagonal entries 
//     * N.B. No optimality guarantees!
//     * 
//     * @return true indicates the map is a row-ordering; false indicates a col-ordering
//     */
//    public static boolean getReorderingForStrongDiagonal(double[][] mat, int[] map){
//      // we prefer permutations here according to the trace of their log, but 
//      // that should yield the same ordering as sorting them with 
//      // the loss function  KL(q || p) where q is a diagonal matrix and 
//      // p consists of normalized row vectors
//      
//      // row heuristic
//      int[] rowPerm = getGreedyRowReorderingForStrongDiagonal(mat);
//      double[][] rowOrdered = clone(mat);
//      reorderRowsToSelf(rowPerm, rowOrdered);
//      double rowGoodness = -1*klDivergenceWithStrongDiagonal(rowOrdered, 1e-6);
//      
//      // col heuristic
//      int[] colPerm = getGreedyColReorderingForStrongDiagonal(mat);
//      double[][] colOrdered = clone(mat);
//      reorderColsToSelf(colPerm, colOrdered);
//      double colGoodness = -1*klDivergenceWithStrongDiagonal(colOrdered, 1-1e-6);
//      
//      // add random permutations? Other heuristics?
//      
//      // retval
//      int[] best = (rowGoodness>colGoodness)? rowPerm: colPerm;
//      for (int i=0; i<best.length; i++){
//        map[i] = best[i];
//      }
//      return rowGoodness>colGoodness;
//    }
    
    /**
     * Return the loss associated with assigning row to the position rowAssignment.
     * Used in finding row permutations.
     */
    public interface RowReorderingLossFunction{
      public double rowAssignmentLoss(double[] row, int rowAssigment);
    }
    
    public static class OffDiagonalSumLoss implements RowReorderingLossFunction{
      /** {@inheritDoc} */
      @Override
      public double rowAssignmentLoss(double[] row, int rowAssignment) {
        return DoubleArrays.sum(row)-row[rowAssignment];
      }
    }
    
    public static class KLDivergenceFromDiagonalLoss implements RowReorderingLossFunction{
      /** {@inheritDoc} */
      @Override
      public double rowAssignmentLoss(double[] row, int rowAssignment) {
        return - Math.log(row[rowAssignment]);
      }
    }

	public static int[] getColReorderingForStrongDiagonal(double[][] confusions) {
		return getRowReorderingForStrongDiagonal(Matrices.transpose(confusions));
	}
	
    public static int[] getRowReorderingForStrongDiagonal(double[][] mat){
        return getRowReordering(mat, new OffDiagonalSumLoss());
    }

    public static int[] getNormalizedRowReorderingForStrongDiagonal(double[][] mat){
      double[][] normalizedMat = Matrices.clone(mat);
      // smooth (eliminate 0s by adding epsilon)
      Matrices.addToSelf(normalizedMat, 1e-6);
      // normalize to probability dist, so KL divergence is applicable
      Matrices.normalizeRowsToSelf(normalizedMat);
      return getRowReordering(normalizedMat, new KLDivergenceFromDiagonalLoss());
    }
    
    /**
     * Finds an ordering that minimizes the passed-in loss function.
     * Formulates the problem as an instance of the 'assignment problem' 
     * and solves using a linear solver.
     * 
     * @param lossfunction takes as an argument a matrix row, and outputs the 
     * loss associated 
     * 
     * @return a vectors of new row assignments. For example, [0,1,2,3] indicate the trivial re-ordering. 
     */
    public static int[] getRowReordering(double[][] mat, RowReorderingLossFunction lossFunction){
      // In the assignment problem formulation, there will be one variable associated with 
      // each possible row->dst assignment. We calculate the coefficient of each of 
      // these using the passed-in loss function. Our coefficients are therefore a 
      // vectorized form of the loss matrix.
      int n=mat.length;
      double[] objCoeff = new double[n*n];
      for (int src=0; src<n; src++){
        for (int dst=0; dst<n; dst++){
          double loss = lossFunction.rowAssignmentLoss(mat[src], dst);
          objCoeff[n*src+dst] = loss;
          if (Double.isInfinite(loss) || Double.isNaN(loss)){
            throw new IllegalArgumentException("loss function returned an invalid number ("+loss+") "
                + "when asked to consider \n\trow "+DoubleArrays.toString(mat[src])+" \n\tin position "+dst);
          }
        }
      }
      
      // objective function
      double offset = 0;
      LinearObjectiveFunction f = new LinearObjectiveFunction(objCoeff, offset);
      
      // constraints
      Collection<LinearConstraint> constraints = Lists.newArrayList();
      // each src must have exactly one dst 
      for (int src=0; src<n; src++){
        double[] constCoeff = DoubleArrays.of(0, n*n);
        Arrays.fill(constCoeff,n*src,n*(src+1),1); // single row of 1s
        constraints.add(new LinearConstraint(constCoeff, Relationship.EQ, 1));
      }
      // each dst must have exactly one src
      for (int dst=0; dst<n; dst++){
        double[] constCoeff = DoubleArrays.of(0, n*n);
        for (int src=0; src<n; src++){
          constCoeff[n*src+dst] = 1; // single col of 1s
        }
        constraints.add(new LinearConstraint(constCoeff, Relationship.EQ, 1));
      }
      
      // solve
      boolean restrictToNonNegative = true;
      SimplexSolver solver = new SimplexSolver();
      solver.setMaxIterations(10000);
      PointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, restrictToNonNegative);
      double[] assignmentMatrix = solution.getPoint();
      
      // the assignment matrix should be very simple; each x is either 0 or 1, 
      // and there is a single 1 per row and column
      // we can deterministically convert this to an int[]
      int[] result = new int[n];
      for (int src=0; src<n; src++){
        int rowNonZeroCount = 0;
        for (int dst=0; dst<n; dst++){
          double val = assignmentMatrix[n*src+dst];
          if (Math.abs(val-1)<1e-6){
            result[src] = dst;
            rowNonZeroCount++;
          }
        }
        if (rowNonZeroCount!=1){
          throw new IllegalStateException("The assignment problem linear solver returned an assignment matrix with "
              + "invalid entries. This should never happen! Here is the matrix encoded as a vector "
              + "with rows of length "+n+":\n\t"+DoubleArrays.toString(assignmentMatrix));
        }
      }
      return result;
    }
    
    /**
     * Re-order rows according to the indices found in rowOrdering
     */
    public static void reorderRowsToSelf(int[] rowOrdering, double[][] mat){
      Preconditions.checkNotNull(rowOrdering);
      Preconditions.checkNotNull(mat);
      Preconditions.checkArgument(mat.length==rowOrdering.length);
      assert isValidMap(rowOrdering); // too expensive for a precondition
      
      double[][] oldMat = new double[mat.length][];
      for (int i=0; i<mat.length; i++){
        oldMat[i] = mat[i];
      }
      for (int i=0; i<mat.length; i++){
        mat[rowOrdering[i]] = oldMat[i];
      }
    }

    /**
     * Re-order columns according to the indices found in colOrdering
     */
    public static void reorderColsToSelf(int[] colOrdering, double[][] mat){
      // not the most efficient implementation, but this isn't 
      // currently being used in any inner loops
      double[][] trans = transpose(mat);
      reorderRowsToSelf(colOrdering, trans);
      double[][] reordered = transpose(trans);
      for (int r=0; r<mat.length; r++){
        mat[r] = reordered[r];
      }
    }
    
    public static void reorderElementsToSelf(int[] colOrdering, double[] arr){
      double[] oldArr = arr.clone();
      for (int i=0; i<arr.length; i++){
        arr[colOrdering[i]] = oldArr[i];
      }
    }

    /**
     * returns the logsum of every element in the matrix 
     */
    public static double logSum(double[][] mat){
      Preconditions.checkNotNull(mat);

      double[] logSums = new double[mat.length];
      for (int r = 0; r < mat.length; r++) {
        logSums[r] = DoubleArrays.logSum(mat[r]);
      }
      return DoubleArrays.logSum(logSums);
    }
    
    /**
     * Computes mat[r][c] = mat[r][c] - logsum(mat)
     */
    public static void logNormalizeToSelf(double[][] mat) {
      double logNormalizer = logSum(mat);
      addToSelf(mat, -logNormalizer);
    }

    /**
     * Returns true iff the dimensions are the same and each element of the vectors are the same
     * within the specified threshold
     */
    public static boolean equals(double[][] mat1, double[][] mat2, double tolerance) {
      if (mat1.length != mat2.length) {
        return false;
      }
      for (int r = 0; r < mat1.length; r++) {
        if (mat1[r].length != mat2[r].length) {
          return false;
        }
        for (int c = 0; c < mat1[r].length; c++) {
          if (!Math2.doubleEquals(mat1[r][c], mat2[r][c], tolerance)) {
            return false;
          }
        }
      }
      return true;
    }

    private static int strWidthOf(double number, int numDigits){
      if (number==0){
        return 1;
      }
      else{
        int asInt = (int)Math.round(number*(Math.pow(10, numDigits)));
        return (int)Math.ceil(Math.log10(asInt+1));
      }
    }

    public static String toString(double[][][] mat) {
    	return toString(mat, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 10);
    }
    public static String toString(double[][][] mat, int maxDim1, int maxDim2, int maxDim3, int numFractionDigits) {
    	StringBuilder bld = new StringBuilder();
    	bld.append("[\n");
    	for (int i=0; i<mat.length; i++){
    		bld.append(toString(mat[i], maxDim2, maxDim3, numFractionDigits));
    		if (i<mat.length-1){
    			bld.append(",\n");
    		}
    		if (i>=maxDim1){
    			bld.append("...");
    			break;
    		}
    	}
    	bld.append("\n]");
    	return bld.toString();
    }
    
    /**
     * Pretty print a matrix
     */
    public static String toString(double[][] mat) {
    	return toString(mat,Integer.MAX_VALUE,Integer.MAX_VALUE);
    }

    public static String toString(double[][] mat, int maxRows, int maxCols) {
      return toString(mat, maxRows, maxCols, 10);
    }

    public static String toString(double[][] mat, int maxRows, int maxCols, int numFractionDigits) {
      // how many digits are in the biggest number in each column?
      int[] maxColNumWidths = Matrices.argMaxesInColumns(mat);
      for (int c=0; c<maxColNumWidths.length; c++){
        maxColNumWidths[c] = strWidthOf(mat[maxColNumWidths[c]][c], numFractionDigits);
      }
      DecimalFormat df = new DecimalFormat();
      df.setMaximumFractionDigits(numFractionDigits);
      df.setMinimumFractionDigits(numFractionDigits);
      df.setGroupingUsed(false);
      
      StringBuilder str = new StringBuilder();
      str.append('[');
      int numrows = Math.min(mat.length, maxRows);
      for (int r = 0; r<numrows; r++) {
        if (r > 0) {
          str.append("\n ");
        }
        str.append('[');
        for (int c = 0; c < Math.min(mat[r].length, maxCols); c++) {
          if (c > 0) {
            str.append(", ");
          }
          int strWidth = strWidthOf(mat[r][c], numFractionDigits);
          for (int fill=strWidth; fill<maxColNumWidths[c]; fill++){
            str.append(' ');
          }
          str.append(df.format(mat[r][c]));
        }
        if (mat[r].length > maxCols) {
          str.append(", ...");
        }
        str.append("]");
        if (r<numrows-1){
        	str.append(",");
        }
      }
      if (mat.length > maxRows) {
        str.append(",\n  ...\n");
      }
      str.append("]");
      return str.toString();
    }

    public static double[][] convertInt2Double(int[][] mat){
      double[][] m2 = new double[mat.length][];
      for (int r=0; r<mat.length; r++){
        m2[r] = new double[mat[r].length];
        for (int c=0; c<mat[r].length; c++){
          m2[r][c] = mat[r][c];
        }
      }
      return m2;
    }

    public static String toString(int[][][] mat) {
    	StringBuilder bld = new StringBuilder();
    	bld.append("[\n");
    	for (int i=0; i<mat.length; i++){
    		bld.append(toString(mat[i]));
    		if (i<mat.length-1){
    			bld.append(",\n");
    		}
    	}
    	bld.append("\n]");
    	return bld.toString();
    }
    
    public static String toString(int[][] mat) {
    	return toString(mat,Integer.MAX_VALUE,Integer.MAX_VALUE);
    }
    
    public static String toString(int[][] mat, int maxRows, int maxCols) {
      // (pfelt) Note: this is quick n easy, but innefficient. 
      return toString(convertInt2Double(mat), maxRows, maxCols, 0);
    }

    public static int[][] parseIntMatrix(String str){
  		return new Gson().fromJson(str, int[][].class);
    }

    public static int[][][] parseIntTensor(String str){
  		return new Gson().fromJson(str, int[][][].class);
    }

    public static double[][] diagonalMatrix(int rows, int cols){
      double[][] m = new double[rows][cols];
      for (int r=0; r<rows; r++){
        for (int c=0; c<cols; c++){
          if (r==c){
            m[r][c] = 1;
          }
        }
      }
      return m;
    }
    
    public static double[][] uniformRowMatrix(int numRows, int numCols){
      double[][] m = Matrices.of(1, numRows, numCols);
      Matrices.normalizeRowsToSelf(m);
      return m;
    }

	public static double[][] unflatten(double[] arr, int numRows, int numCols) {
		double[][] result = new double[numRows][numCols];
		for (int r=0; r<numRows; r++){
			for (int c=0; c<numCols; c++){
				result[r][c] = arr[r*numCols+c];
			}
		}
		return result;
	}
	
    public static double[] flatten(double[][] matrix){
      if (matrix==null){
        return null;
      }
      int height = matrix.length;
      if (height==0){
        return new double[]{};
      }
      Preconditions.checkNotNull(matrix[0]);
      int width = matrix[0].length;
      if (width==0){
        return new double[]{};
      }
      double[] array = new double[height * width];
      
      for (int r=0; r<matrix.length; r++){
        double[] row = matrix[r];
        Preconditions.checkArgument(row.length==width, "matrix must be square");
        for (int c=0; c<row.length; c++){
          array[(r*width)+c] = matrix[r][c];
        }
      }
      return array;
    }

    public static double[][][] fromInts(int[][]... rows){
        double[][][] result = new double[rows.length][][];
        for (int r=0; r<rows.length; r++){
          result[r] = fromInts(rows[r]);
        }
        return result;
    }
    
    public static double[][] fromInts(int[]... rows){
      double[][] result = new double[rows.length][];
      for (int r=0; r<rows.length; r++){
        result[r] = DoubleArrays.fromInts(rows[r]);
      }
      return result;
    }

    public static double[][] fromArrays(double[]... rows){
      double[][] result = new double[rows.length][];
      for (int r=0; r<rows.length; r++){
        result[r] = rows[r];
      }
      return result;
    }

    public static void multiplyToSelf(double[][][] tensor, double value){
    	for (int r=0; r<tensor.length; r++){
    		multiplyToSelf(tensor[r], value);
    	}
    }
    
    public static void multiplyToSelf(double[][] mat, double value){
    	for (int r=0; r<mat.length; r++){
    		for (int c=0; c<mat[r].length; c++){
    			mat[r][c] *= value;
    		}
    	}
    }

	public static double[][] sumOverFirst(double[][][] tensor) {
		Preconditions.checkNotNull(tensor);
		if (tensor.length == 0 || tensor[0].length==0) {
			return new double[0][0];
		}
		int dim1 = tensor.length;
		int dim2 = tensor[0].length;
		int dim3 = tensor[0][0].length;
		
		double[][] sum = new double[dim2][dim3];
		for (int i = 0; i < dim1; i++) {
			Preconditions.checkArgument(tensor[i].length == dim2, "tensor must not be ragged");
			for (int j = 0; j < dim2; j++) {
				Preconditions.checkArgument(tensor[i][j].length == dim3, "tensor must not be ragged");
				for (int k=0; k<dim3; k++){
					sum[j][k] += tensor[i][j][k];
				}
			}
		}
		return sum;
	}

	public static double[][] parseMatrix(String str) {
		return new Gson().fromJson(str, double[][].class);
	}
	
	public static double[][][] parseTensor(String str) {
		return new Gson().fromJson(str, double[][][].class);
	}

	public static int[][] selectSecondDimension(int[][][] arr, int index) {
		Preconditions.checkNotNull(arr);
		Preconditions.checkArgument(arr.length>0);
		Preconditions.checkNotNull(arr[0]);
		Preconditions.checkArgument(arr[0].length>0);
		Preconditions.checkNotNull(arr[0][0]);
		Preconditions.checkArgument(arr[0][0].length>0);
		
		int[][] result = new int[arr.length][arr[0][0].length];
		for (int i=0; i<arr.length; i++){
			result[i] = arr[i][index];
		}
		return result;
	}

	  public static long sum(int[][][] arr) {
	    long sum = 0;
	    for (int i=0; i<arr.length; i++){
	      sum += sum(arr[i]);
	    }
	    return sum;
	  }
	  
	  public static long sum(int[][] arr) {
	    long sum = 0;
	    for (int i=0; i<arr.length; i++){
	      sum += IntArrays.sum(arr[i]);
	    }
	    return sum;
	  }

	public static void multiplyAndRoundToSelf(int[][] arr, double value){
		for (int i=0; i<arr.length; i++){
			IntArrays.multiplyAndRoundToSelf(arr[i], value);
		}
	}

	public static int max(int[][][] matrix){
		ArgMinMaxTracker<Integer, ?> tracker = new ArgMinMaxTracker<Integer,Integer>();
		for (int[][] row: matrix){
			tracker.offer(max(row));
		}
		return tracker.max();
	}
	
	public static int max(int[][] matrix){
		ArgMinMaxTracker<Integer, ?> tracker = new ArgMinMaxTracker<Integer,Integer>();
		for (int[] row: matrix){
			for (int val: row){
				tracker.offer(val);
			}
		}
		return tracker.max();
	}

	public static double max(double[][][] matrix){
		ArgMinMaxTracker<Double, ?> tracker = new ArgMinMaxTracker<Double,Integer>();
		for (double[][] row: matrix){
			tracker.offer(max(row));
		}
		return tracker.max();
	}
	
	public static double max(double[][] matrix){
		ArgMinMaxTracker<Double, ?> tracker = new ArgMinMaxTracker<Double,Integer>();
		for (double[] row: matrix){
			for (double val: row){
				tracker.offer(val);
			}
		}
		return tracker.max();
	}

	/**
	 * Combine rows via majority vote. Ties are split randomly.
	 */
	public static int[] aggregateRowsViaMajorityVote(int[][] matrix, RandomGenerator rnd) {
		Preconditions.checkNotNull(matrix);
		Preconditions.checkArgument(matrix.length > 0);
		int assignments[] = new int[matrix[0].length];
		// calculate marginals
		int numValues = max(matrix)+1;
		IntArrayCounter counter = new IntArrayCounter(assignments.length, numValues);
		for (int[] row: matrix) {
			counter.increment(row);
		}
		// assign maxes
		for (int v = 0; v < assignments.length; v++) {
			assignments[v] = counter.argmax(v);
		}
		return assignments;
	}

	/**
	 * Combine matrices (indexed by the 1st dimension) via majority vote. Ties are split randomly.
	 * Matrices may be ragged as long as the raggedness is the same for each.
	 */
	public static int[][] aggregateFirstDimensionViaMajorityVote(int[][][] matrix, RandomGenerator rnd) {
		Preconditions.checkNotNull(matrix);
		Preconditions.checkArgument(matrix.length > 0);
		Preconditions.checkArgument(matrix[0].length > 0);
		
		int dim1 = matrix.length;
		int dim2 = matrix[0].length;
		int[][] assignments = new int[dim2][];
		int numValues = max(matrix)+1;
		// calculate marginals for each row
		for (int d2=0; d2<dim2; d2++){
			int dim3 = matrix[0][d2].length; // allow ragged inner dimension, but must be same across all matrices (indexed by d1)
			assignments[d2] = new int[dim3]; 
			IntArrayCounter counter = new IntArrayCounter(assignments.length, numValues);
			for (int d1=0; d1<dim1; d1++){
				counter.increment(matrix[d1][d2]);
				// assign maxes
				for (int d3 = 0; d3 < dim3; d3++) {
					assignments[d2][d3] = counter.argmax(d3);
				}
			}
		}
		return assignments;
	}

}
