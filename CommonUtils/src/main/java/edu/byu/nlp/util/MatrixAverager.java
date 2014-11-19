/**
 * Copyright 2013 Brigham Young University
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

import java.util.Arrays;

/**
 * Maintain average double values over a matrix of values
 */
public class MatrixAverager {
//  private static final Logger logger = Logger.getLogger(DoubleArrayAverager.class.getName());
  
  // assignment counts
  DoubleArrayAverager[] data;
  private int numCols;
  private int numRows;

  public MatrixAverager(int rows, int cols) {
    this.numRows=rows;
    this.numCols=cols;
    reset();
  }

  public void reset() {
    data = new DoubleArrayAverager[numRows];
    for (int r=0; r<numRows; r++){
      data[r] = new DoubleArrayAverager(numCols);
    }
  }

  public double[][] average(){
    double[][] result = new double[numRows][];
    for (int r=0; r<numRows; r++){
      result[r] = average(r);
    }
    return result;
  }

  public double[] average(int row){
    return data[row].average();
  }

  public void increment(double[][] vals) {
    for (int r=0; r<numRows; r++){
      increment(r, vals[r]);
    }
  }
  
  public void increment(int row, double[] vals) {
    data[row].increment(vals);
  }
  
  /** {@inheritDoc} */
  @Override
  public String toString() {
    return Arrays.toString(average());
  }
}