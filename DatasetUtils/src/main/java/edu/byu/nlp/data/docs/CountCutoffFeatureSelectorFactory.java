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
import java.util.Map;

import edu.byu.nlp.data.streams.DataStreamSink;
import edu.byu.nlp.data.types.DataStreamInstance;
import edu.byu.nlp.data.types.SparseFeatureVector;
import edu.byu.nlp.data.types.SparseFeatureVector.Entry;

/**
 * A {@code FeatureSelectorFactory} that creates a feature selector who retains all features that
 * occur more than a pre-specified number of times.
 * 
 * @author rah67
 * 
 */
public class CountCutoffFeatureSelectorFactory<L> implements FeatureSelectorFactory {

  private final int cutoff;

  public CountCutoffFeatureSelectorFactory(int cutoff) {
    this.cutoff = cutoff;
  }

  @Override
  public DataStreamSink<BitSet> newFeatureSelector(int numFeatures) {
    return new CountCutoffFeatureSelector<L>(cutoff, numFeatures);
  }

  public static class CountCutoffFeatureSelector<L> implements DataStreamSink<BitSet> {

    private final int cutoff;
    private final int numFeatures;

    public CountCutoffFeatureSelector(int cutoff, int numFeatures) {
      this.cutoff = cutoff;
      this.numFeatures = numFeatures;
    }

    /** {@inheritDoc} */
    @Override
	public BitSet process(Iterable<Map<String, Object>> docs) {
      return buildBitSet(countFeatures(docs));
    }

    private double[] countFeatures(Iterable<Map<String, Object>> docs) {
      double[] counts = new double[numFeatures];
      for (Map<String, Object> doc : docs) {
    	  if (DataStreamInstance.isLabel(doc)){ // ignore annotations; no data
	        for (Entry e : ((SparseFeatureVector)DataStreamInstance.getData(doc)).sparseEntries()) {
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
