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
package edu.byu.nlp.data.docs;

import java.util.BitSet;

import edu.byu.nlp.data.pipes.DataSink;
import edu.byu.nlp.data.types.SparseFeatureVector;

/**
 * @author rah67
 *
 */
public interface FeatureSelectorFactory<L> {
	DataSink<SparseFeatureVector, L, BitSet> newFeatureSelector(int numFeatures);
}
