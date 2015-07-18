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
import java.util.Map;

import com.google.common.collect.Lists;

import edu.byu.nlp.data.streams.DataStreamSink;

/**
 * Utility class for {@code FeatureSelectorFactory}.
 * 
 * @author rah67
 *
 */
public class FeatureSelectorFactories {
  private FeatureSelectorFactories() {}
  
  private static class ConjoinedFeatureSelectorFactory implements FeatureSelectorFactory {
    private final FeatureSelectorFactory factory1;
    private final FeatureSelectorFactory factory2;

    ConjoinedFeatureSelectorFactory(FeatureSelectorFactory factory1,
                                    FeatureSelectorFactory factory2) {
      this.factory1 = factory1;
      this.factory2 = factory2;
    }

    private static class ConjoinedFeatureSelector implements DataStreamSink<BitSet> {
      private final DataStreamSink<BitSet> selector1;
      private final DataStreamSink<BitSet> selector2;
      
      ConjoinedFeatureSelector(DataStreamSink<BitSet> selector1,
                               DataStreamSink<BitSet> selector2) {
        this.selector1 = selector1;
        this.selector2 = selector2;
      }

      /** {@inheritDoc} */
      @Override
      public BitSet process(Iterable<Map<String, Object>> data) {
        BitSet bitSet = new BitSet();
        bitSet.or(selector1.process(data));
        bitSet.and(selector2.process(data));
        return bitSet;
      }

    }

    /** {@inheritDoc} */
    @Override
    public DataStreamSink<BitSet> newFeatureSelector(int numFeatures) {
      return new ConjoinedFeatureSelector(factory1.newFeatureSelector(numFeatures),
          factory2.newFeatureSelector(numFeatures));
    }
  }

  @SafeVarargs
public static <L> FeatureSelectorFactory conjoin(FeatureSelectorFactory... factories) {
    ArrayList<FeatureSelectorFactory> nonNullFactories = Lists.newArrayList();
    for (FeatureSelectorFactory factory: factories){
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
      FeatureSelectorFactory factory = nonNullFactories.get(0);
      for (int i=1; i<nonNullFactories.size(); i++){
        factory = new ConjoinedFeatureSelectorFactory(factory,nonNullFactories.get(i));
      }
      return factory; 
    }
  }
}
