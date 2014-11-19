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
package edu.byu.nlp.data.docs;

import java.util.ArrayList;
import java.util.BitSet;

import com.google.common.collect.Lists;

import edu.byu.nlp.data.FlatInstance;
import edu.byu.nlp.data.pipes.DataSink;
import edu.byu.nlp.data.types.SparseFeatureVector;

/**
 * Utility class for {@code FeatureSelectorFactory}.
 * 
 * @author rah67
 *
 */
public class FeatureSelectorFactories {
  private FeatureSelectorFactories() {}
  
  private static class ConjoinedFeatureSelectorFactory<L> implements FeatureSelectorFactory<L> {
    private final FeatureSelectorFactory<L> factory1;
    private final FeatureSelectorFactory<L> factory2;

    ConjoinedFeatureSelectorFactory(FeatureSelectorFactory<L> factory1,
                                    FeatureSelectorFactory<L> factory2) {
      this.factory1 = factory1;
      this.factory2 = factory2;
    }

    private static class ConjoinedFeatureSelector<L> implements DataSink<SparseFeatureVector, L, BitSet> {
      private final DataSink<SparseFeatureVector, L, BitSet> selector1;
      private final DataSink<SparseFeatureVector, L, BitSet> selector2;
      
      ConjoinedFeatureSelector(DataSink<SparseFeatureVector, L, BitSet> selector1,
                               DataSink<SparseFeatureVector, L, BitSet> selector2) {
        this.selector1 = selector1;
        this.selector2 = selector2;
      }

      /** {@inheritDoc} */
      @Override
      public BitSet processLabeledInstances(Iterable<FlatInstance<SparseFeatureVector, L>> data) {
        BitSet bitSet = new BitSet();
        bitSet.or(selector1.processLabeledInstances(data));
        bitSet.and(selector2.processLabeledInstances(data));
        return bitSet;
      }
    }

    /** {@inheritDoc} */
    @Override
    public DataSink<SparseFeatureVector, L, BitSet> newFeatureSelector(int numFeatures) {
      return new ConjoinedFeatureSelector<L>(factory1.newFeatureSelector(numFeatures),
          factory2.newFeatureSelector(numFeatures));
    }
  }

  public static <L> FeatureSelectorFactory<L> conjoin(FeatureSelectorFactory<L>... factories) {
    ArrayList<FeatureSelectorFactory<L>> nonNullFactories = Lists.newArrayList();
    for (FeatureSelectorFactory<L> factory: factories){
      if (factory!=null){
        nonNullFactories.add(factory);
      }
    }
    // null factory
    if (nonNullFactories.size()==0){
      return null;
    }
    // chained factories
    else{
      FeatureSelectorFactory<L> factory = nonNullFactories.get(0);
      for (int i=1; i<nonNullFactories.size(); i++){
        factory = new ConjoinedFeatureSelectorFactory<L>(factory,nonNullFactories.get(i));
      }
      return factory; 
    }
  }
}
