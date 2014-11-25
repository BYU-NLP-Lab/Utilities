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
package edu.byu.nlp.pipes.doc;

import java.util.BitSet;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import edu.byu.nlp.annotationinterface.java.AnnotationInterfaceJavaUtils;
import edu.byu.nlp.data.FlatInstance;
import edu.byu.nlp.data.FlatLabeledInstance;
import edu.byu.nlp.data.docs.CountCutoffFeatureSelectorFactory;
import edu.byu.nlp.data.pipes.DataSink;
import edu.byu.nlp.data.types.SparseFeatureVector;
import edu.byu.nlp.dataset.BasicSparseFeatureVector;

/**
 * @author rah67
 *
 */
public class CountCutoffFeatureSelectorFactoryTest {

  private Iterable<FlatInstance<SparseFeatureVector, String>> exampleData() {
    ImmutableList.Builder<FlatInstance<SparseFeatureVector, String>> builder = ImmutableList.builder();
    builder.add(new FlatLabeledInstance<SparseFeatureVector, String>(
    		AnnotationInterfaceJavaUtils.newLabeledInstance(
    				(SparseFeatureVector)new BasicSparseFeatureVector(new int[] {0, 1, 2}, new double[] {1.0, 0.0, 2.0}), "a", "dummy source", false)));
    builder.add(new FlatLabeledInstance<SparseFeatureVector, String>(
    		AnnotationInterfaceJavaUtils.newLabeledInstance(
    				(SparseFeatureVector)new BasicSparseFeatureVector(new int[] {0, 3, 4}, new double[] {1.0, 1.0, 1.0}), "a", "dummy source", false)));
    return builder.build();
  }
  
  /**
   * Test method for {@link edu.byu.nlp.data.docs.CountCutoffFeatureSelectorFactory#newFeatureSelector(int)}.
   */
  @Test
  public void testNewFeatureSelector() {
    CountCutoffFeatureSelectorFactory<String> factory = new CountCutoffFeatureSelectorFactory<String>(1);
    DataSink<SparseFeatureVector, String, BitSet> selector = factory.newFeatureSelector(10);
    BitSet bitSet = selector.processLabeledInstances(exampleData());
    Assertions.assertThat(bitSet.get(0)).isTrue();
    Assertions.assertThat(bitSet.get(1)).isFalse();
    Assertions.assertThat(bitSet.get(2)).isTrue();
    Assertions.assertThat(bitSet.get(3)).isFalse();
    Assertions.assertThat(bitSet.get(4)).isFalse();
  }

}
