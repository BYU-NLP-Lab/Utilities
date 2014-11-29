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
package edu.byu.nlp.dataset;

import edu.byu.nlp.data.types.SparseFeatureVector;
import edu.byu.nlp.data.types.SparseFeatureVector.EntryVisitor;
import edu.byu.nlp.util.Integers;

/**
 * @author rah67
 *
 */
public class SparseFeatureVectors {

	public static interface ValueFunction {
		double apply(int index, double val);
	}
	
	private SparseFeatureVectors() { }
	
	public static SparseFeatureVector transformValues(SparseFeatureVector v, ValueFunction f) {
		SparseFeatureVector copy = v.copy();
		copy.transformValues(f);
		return copy;
	}

  public static void normalizeToSelf(SparseFeatureVector data) {
    final double total = data.sum();
    data.transformValues(new ValueFunction() {
      @Override
      public double apply(int index, double val) {
        return val/total;
      }
    });
  }

  public static void multiplyToSelf(SparseFeatureVector data, final double number){
    data.transformValues(new ValueFunction() {
      @Override
      public double apply(int index, double val) {
        return val*number;
      }
    });
  }
  
  /**
   * Translate a feature vector into a sequence of 
   * feature identities. Fails if features 
   * are not integer-valued. The ordering of the 
   * returned features is arbitrary.
   */
  public static int[] asSequentialIndices(SparseFeatureVector vec){
    final int[] words = new int[Integers.fromDouble(vec.sum(), Datasets.INT_CAST_THRESHOLD)];
    
    vec.visitSparseEntries(new EntryVisitor() {
      @Override
      public void visitEntry(int index, double value) {
        int numberOfOccurencesOfWord = Integers.fromDouble(value, Datasets.INT_CAST_THRESHOLD);
        for (int i=0; i<numberOfOccurencesOfWord; i++){
          words[i++] = index;
        }
      }
    });
    
    return words;
  }

}
