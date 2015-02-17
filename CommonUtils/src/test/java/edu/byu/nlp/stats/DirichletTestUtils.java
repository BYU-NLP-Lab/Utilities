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
package edu.byu.nlp.stats;


import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import com.google.common.collect.Lists;

import edu.byu.nlp.util.DoubleArrays;

/**
 * @author plf1
 *
 */
public class DirichletTestUtils {
	
	private DirichletTestUtils() { }

	public static double[][][] sampleDirichletMultinomialMatrixDataset(double diagonalAlpha, double offdiagonalAlpha, int matrixSize, int numDataPoints, int datumSize, RandomGenerator rnd){
		double[][] alpha = new double[matrixSize][matrixSize];
		for (int r=0; r<matrixSize; r++){
			for (int c=0; c<matrixSize; c++){
				alpha[r][c] = (r==c)? diagonalAlpha: offdiagonalAlpha;
			}
		}
		List<double[][]> result = Lists.newArrayList();
		for (int i=0; i<numDataPoints; i++){
			result.add(sampleDirichletMultinomialMatrixDataset(alpha, datumSize, rnd));
		}
		return result.toArray(new double[0][0][0]);
	}
	
	public static double[][] sampleDirichletMultinomialMatrixDataset(double[][] alpha, int datumSize, RandomGenerator rnd){
		double[][] result = new double[alpha.length][];
		for (int r=0; r<alpha.length; r++){
			result[r] = sampleMultinomialDataset(alpha[r], 1, datumSize, rnd)[0];
		}
		return result;
	}
	
	public static double[][] sampleMultinomialDataset(double[] alpha, int numDataPoints, int datumSize, RandomGenerator rnd){
		double[][] dataset = new double[numDataPoints][];
		for (int i=0; i<numDataPoints; i++){
			double[] p = DirichletDistribution.sample(alpha, rnd);
			dataset[i] = RandomGenerators.nextVectorUnnormalizedProbs(rnd, p, datumSize);
		}
		return dataset;
	}
	
	/**
	 * Returns a 3 x 3 matrix of log probability vectors.
	 */
	public static double[][] sampleDataset() {
		double[][] sampleData = new double[][] { { 0.7, 0.2, 0.1 },
												 { 0.3, 0.4, 0.3 },
												 { 0.6, 0.3, 0.1 } };
		for (int i = 0; i < sampleData.length; i++) {
			DoubleArrays.logToSelf(sampleData[i]);
		}
		return sampleData;
	}
	
}
