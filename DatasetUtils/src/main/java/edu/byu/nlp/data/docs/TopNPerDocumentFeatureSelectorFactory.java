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
import java.util.Comparator;

import edu.byu.nlp.data.FlatInstance;
import edu.byu.nlp.data.pipes.DataSink;
import edu.byu.nlp.data.pipes.Pipes;
import edu.byu.nlp.data.types.SparseFeatureVector;
import edu.byu.nlp.data.types.SparseFeatureVector.Entry;
import edu.byu.nlp.dataset.BasicSparseFeatureVector;
import edu.byu.nlp.util.Heaps;

/**
 * @author rah67
 *
 */
public class TopNPerDocumentFeatureSelectorFactory<L> implements FeatureSelectorFactory<L> {
	
	private final int minFeaturesToKeepPerDocument;

	public TopNPerDocumentFeatureSelectorFactory(int minFeaturesToKeepPerDocument) {
		this.minFeaturesToKeepPerDocument = minFeaturesToKeepPerDocument;
	}

	@Override
	public DataSink<SparseFeatureVector, L, BitSet> newFeatureSelector(int numFeatures) {
		return new TopNPerDocumentFeatureSelector<L>(minFeaturesToKeepPerDocument, numFeatures);
	}
	
	public static class TopNPerDocumentFeatureSelector<L> implements DataSink<SparseFeatureVector, L, BitSet> {
	
		private final int minFeaturesToKeepPerDocument;
		private final int numFeatures;
		
		public TopNPerDocumentFeatureSelector(int minFeaturesToKeepPerDocument, int numFeatures) {
			this.minFeaturesToKeepPerDocument = minFeaturesToKeepPerDocument;
			this.numFeatures = numFeatures;
		}
		
		/** {@inheritDoc} */
		@Override
		public BitSet processLabeledInstances(Iterable<FlatInstance<SparseFeatureVector, L>> docs) {
			double[] logDf = new LogDocumentFrequency<L>(numFeatures).processLabeledInstances(docs);
			
			Iterable<FlatInstance<SparseFeatureVector, L>> tfidfVectors = Pipes.<SparseFeatureVector, SparseFeatureVector, L>labeledInstanceDataTransformingPipe(
					new CountsToTFIDF<String>(logDf)).apply(docs);
//			Iterable<FlatInstance<BasicSparseFeatureVector, L>> tfidfVectors =
//					Instances.<L, SparseFeatureVector, SparseFeatureVector>transformedLabeledInstance(docs,
//							new CountsToTFIDF<String>(logDf));
			
			return buildBitSet(tfidfVectors);
		}
	
		private BitSet buildBitSet(Iterable<FlatInstance<SparseFeatureVector, L>> tfidfVectors) {
			BitSet b = new BitSet(numFeatures);
			Comparator<Entry> c = BasicSparseFeatureVector.valueComparator();
			for (FlatInstance<SparseFeatureVector, L> doc : tfidfVectors) {
				for (Entry e : Heaps.largestN(doc.getData().sparseEntries(), minFeaturesToKeepPerDocument, false, c)) {
					b.set(e.getIndex());
				}
			}
			return b;
		}

	}

}