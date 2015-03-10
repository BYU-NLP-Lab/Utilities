/**
 * Copyright 2014 Brigham Young University
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

import edu.byu.nlp.data.types.SparseFeatureVector;
import edu.byu.nlp.dataset.SparseFeatureVectors;

/**
 * @author pfelt
 *
 */
public class CountNormalizer implements Function<SparseFeatureVector, SparseFeatureVector> {

  private Integer featureNormalizationConstant;

  /**
   * @param featureNormalizationConstant
   */
  public CountNormalizer(Integer featureNormalizationConstant) {
    this.featureNormalizationConstant=featureNormalizationConstant;
  }

  /** {@inheritDoc} */
  @Override
  public SparseFeatureVector apply(SparseFeatureVector fv) {
    // note: cloning for safety and to respect pipeline semantics
    // but it's probably not necessary in most situations
    SparseFeatureVector newFv = fv.copy(); 
    if (featureNormalizationConstant!=null){
      double docTotal = newFv.sum();
      double scalingFactor = featureNormalizationConstant/docTotal;
      SparseFeatureVectors.multiplyToSelf(newFv, scalingFactor);
    }
    return newFv;
  }

}
