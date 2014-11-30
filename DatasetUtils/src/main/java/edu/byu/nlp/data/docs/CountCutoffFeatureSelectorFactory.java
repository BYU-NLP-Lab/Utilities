/**
 * Copyright 2012 Brigham Young University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.byu.nlp.data.docs;

import java.util.BitSet;

import edu.byu.nlp.data.FlatInstance;
import edu.byu.nlp.data.pipes.DataSink;
import edu.byu.nlp.data.types.SparseFeatureVector;
import edu.byu.nlp.data.types.SparseFeatureVector.Entry;

/**
 * A {@code FeatureSelectorFactory} that creates a feature selector who retains all features that
 * occur more than a pre-specified number of times.
 * 
 * @author rah67
 * 
 */
public class CountCutoffFeatureSelectorFactory<L> implements FeatureSelectorFactory<L> {

  private final int cutoff;

  public CountCutoffFeatureSelectorFactory(int cutoff) {
    this.cutoff = cutoff;
  }

  @Override
  public DataSink<SparseFeatureVector, L, BitSet> newFeatureSelector(int numFeatures) {
    return new CountCutoffFeatureSelector<L>(cutoff, numFeatures);
  }

  public static class CountCutoffFeatureSelector<L>
      implements
        DataSink<SparseFeatureVector, L, BitSet> {

    private final int cutoff;
    private final int numFeatures;

    public CountCutoffFeatureSelector(int cutoff, int numFeatures) {
      this.cutoff = cutoff;
      this.numFeatures = numFeatures;
    }

    /** {@inheritDoc} */
    @Override
	public BitSet processLabeledInstances(Iterable<FlatInstance<SparseFeatureVector, L>> docs) {
      return buildBitSet(countFeatures(docs));
    }

    private double[] countFeatures(Iterable<FlatInstance<SparseFeatureVector, L>> docs) {
      double[] counts = new double[numFeatures];
      for (FlatInstance<SparseFeatureVector, L> doc : docs) {
    	  if (!doc.isAnnotation()){ // ignore annotations; no data
	        for (Entry e : doc.getData().sparseEntries()) {
	          counts[e.getIndex()] += e.getValue();
	        }
    	  }
      }
      return counts;
    }

    private BitSet buildBitSet(double[] counts) {
      BitSet b = new BitSet(numFeatures);
      for (int i = 0; i < counts.length; i++) {
        if (counts[i] > cutoff) {
          b.set(i);
        }
      }
      return b;
    }


  }
}
