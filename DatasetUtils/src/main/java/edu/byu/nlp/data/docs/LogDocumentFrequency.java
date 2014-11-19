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

import edu.byu.nlp.data.FlatInstance;
import edu.byu.nlp.data.pipes.DataSink;
import edu.byu.nlp.data.types.SparseFeatureVector;

/**
 * @author rah67
 *
 */
public class LogDocumentFrequency<L> implements DataSink<SparseFeatureVector, L, double[]> {
	
	public final int numFeatures;
	
	public LogDocumentFrequency(int numFeatures) {
		this.numFeatures = numFeatures;
	}
	
	private static class DFIncrementor implements SparseFeatureVector.IndexVisitor {

		private double[] df;
		
		public DFIncrementor(int numFeatures) {
			this.df = new double[numFeatures];
		}
		
		@Override
		public void visitIndex(int index) {
			++df[index];
		}
	}
	
	/** {@inheritDoc} */
	@Override
	public double[] processLabeledInstances(Iterable<FlatInstance<SparseFeatureVector, L>> documents) {
		DFIncrementor inc = new DFIncrementor(numFeatures);
		for (FlatInstance<SparseFeatureVector, L> document : documents) {
			document.getData().visitIndices(inc);
		}
		// Take the log
		for (int i = 0; i < inc.df.length; i++) {
			inc.df[i] = Math.log(inc.df[i]);
		}
		return inc.df;
	}

}
