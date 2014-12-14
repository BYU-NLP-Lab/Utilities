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

import com.google.common.base.Function;
import com.google.common.primitives.Doubles;

import edu.byu.nlp.data.types.SparseFeatureVector;
import edu.byu.nlp.dataset.SparseFeatureVectors;
import edu.byu.nlp.dataset.SparseFeatureVectors.ValueFunction;

/**
 * @author rah67
 *
 */
public class CountsToTFIDF<E> implements Function<SparseFeatureVector, SparseFeatureVector> {
	
	private final double[] logDf;
	private double numDocuments;
	
	public CountsToTFIDF(double[] logDf, int numDocuments) {
		this.logDf = logDf;
		this.numDocuments = numDocuments;
	}
	
	/** {@inheritDoc} */
	@Override
	public SparseFeatureVector apply(SparseFeatureVector features) {
		final double logNumDocuments = Math.log(numDocuments);
		return SparseFeatureVectors.transformValues(features, new ValueFunction() {
			@Override public double apply(int index, double val) {
				double logIdf = logNumDocuments - logDf[index];
				assert Doubles.isFinite(val * logIdf);
				return val  * logIdf;
			}
		});
	}

}
