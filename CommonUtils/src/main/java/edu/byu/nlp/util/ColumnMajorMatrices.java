/**
 * Copyright 2014 Brigham Young University
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

import com.google.common.base.Preconditions;

/**
 * @author pfelt
 *
 * Utility class for operations on matrices
 * encoded as column major arrays: double[]  
 */
public class ColumnMajorMatrices {

  public static void normalizeRows(double[] weights, int numRows){
    Preconditions.checkNotNull(weights);
    Preconditions.checkArgument(weights.length%numRows==0);
    
    int rowLength = weights.length/numRows;
    double[] rowSums = new double[numRows];
    int index = 0;
    for (int col = 0; col < rowLength; col++) {
      for (int row = 0; row < numRows; row++) {
        rowSums[row] += weights[index++]; 
      }
    }
    
    index = 0;
    for (int col = 0; col < rowLength; col++) {
      for (int row = 0; row < rowSums.length; row++) {
        weights[index++] /= rowSums[row]; 
      }
    }
  }
  
}
