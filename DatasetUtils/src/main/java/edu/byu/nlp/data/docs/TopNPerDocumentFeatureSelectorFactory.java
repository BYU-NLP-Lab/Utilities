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
import java.util.Map;

import com.google.common.collect.Iterables;

import edu.byu.nlp.data.streams.DataStreamSink;
import edu.byu.nlp.data.streams.DataStreams;
import edu.byu.nlp.data.streams.DataStreams.Transform;
import edu.byu.nlp.data.types.DataStreamInstance;
import edu.byu.nlp.data.types.SparseFeatureVector;
import edu.byu.nlp.data.types.SparseFeatureVector.Entry;
import edu.byu.nlp.dataset.BasicSparseFeatureVector;
import edu.byu.nlp.util.Heaps;

/**
 * @author rah67
 *
 */
public class TopNPerDocumentFeatureSelectorFactory implements FeatureSelectorFactory {
	
	private final int minFeaturesToKeepPerDocument;

	public TopNPerDocumentFeatureSelectorFactory(int minFeaturesToKeepPerDocument) {
		this.minFeaturesToKeepPerDocument = minFeaturesToKeepPerDocument;
	}

	@Override
	public DataStreamSink<BitSet> newFeatureSelector(int numFeatures) {
		return new TopNPerDocumentFeatureSelector(minFeaturesToKeepPerDocument, numFeatures);
	}
	
	public static class TopNPerDocumentFeatureSelector implements DataStreamSink<BitSet> {
	
		private final int minFeaturesToKeepPerDocument;
		private final int numFeatures;
		
		public TopNPerDocumentFeatureSelector(int minFeaturesToKeepPerDocument, int numFeatures) {
			this.minFeaturesToKeepPerDocument = minFeaturesToKeepPerDocument;
			this.numFeatures = numFeatures;
		}
		
		/** {@inheritDoc} */
		@Override
    public BitSet process(Iterable<Map<String, Object>> docs) {
			double[] logDf = new LogDocumentFrequency(numFeatures).process(docs);
			int numDocuments = Iterables.size(docs); // is there somewhere more efficient to do this naturally? 
			
			Transform dataTransformer = DataStreams.Transforms.transformFieldValue(DataStreamInstance.DATA, new CountsToTFIDF<String>(logDf, numDocuments));
			Iterable<Map<String,Object>> tfidfVectors = Iterables.transform(docs, dataTransformer);
			
			return buildBitSet(tfidfVectors);
		}
	
		private BitSet buildBitSet(Iterable<Map<String,Object>> tfidfVectors) {
			BitSet b = new BitSet(numFeatures);
			Comparator<Entry> c = BasicSparseFeatureVector.valueComparator();
			for (Map<String,Object> doc : tfidfVectors) {
				if (DataStreamInstance.getData(doc)!=null){
				  SparseFeatureVector data = (SparseFeatureVector) DataStreamInstance.getData(doc);
					for (Entry e : Heaps.largestN(data.sparseEntries(), minFeaturesToKeepPerDocument, false, c)) {
						b.set(e.getIndex());
					}
				}
			}
			return b;
		}

	}

}