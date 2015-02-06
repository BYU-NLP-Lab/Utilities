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


import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import edu.byu.nlp.util.DoubleArrays;


/**
 * @author rah67
 *
 */
public class RandomGenerators {

	private static final double LOGSUM_THRESHOLD = 20.0;
	@VisibleForTesting static final int BINARY_SEARCH_THRESHOLD = 5;
	
	/**
	 * This routine generates a random number between 0 and n inclusive, following
	 * the binomial distribution with probability p and n trials. The routine is
	 * based on the BTPE algorithm, described in:
	 * 
	 * Voratas Kachitvichyanukul and Bruce W. Schmeiser:
	 * Binomial Random Variate Generation
	 * Communications of the ACM, Volume 31, Number 2, February 1988, pages 216-222.
	 * 
	 * Snagged on March 19, 2009 from:
	 * 
	 * http://chianti.ucsd.edu/svn/csplugins/trunk/ucsf/scooter/clusterMaker/src/clusterMaker/algorithms/kmeans/KCluster.java
	 * 
	 * by Robbie who also converted the uniform() calls to RandomGenerator.nextDouble()
	 * 
	 * @param p The probability of a single event.  This should be less than or equal to 0.5.
	 * @param n The number of trials
	 * @return An integer drawn from a binomial distribution with parameters (p, n).
	 */
	public static int nextBinom(RandomGenerator rnd, int n, double p) {
		// rah67 March 19, 2007; didn't properly handle the degenerate case
		if (p == 1.0) return n;
		
		double q = 1 - p;
		if (n*p < 30.0) /* Algorithm BINV */
		{ 
			double s = p/q;
			double a = (n+1)*s;
//			double r = Math.exp(n*Math.log(q)); /* pow() causes a crash on AIX */
			double r = Math.pow(q,n); /* rah67 March 19, 2007 */
			int x = 0;
			double u = rnd.nextDouble();
			while(true)
			{ 
				if (u < r) return x;
				u-=r;
				x++;
				r *= (a/x)-s;
			}
		}
		else /* Algorithm BTPE */
		{ /* Step 0 */
			double fm = n*p + p;
			int m = (int) fm;
			double p1 = Math.floor(2.195*Math.sqrt(n*p*q) -4.6*q) + 0.5;
			double xm = m + 0.5;
			double xl = xm - p1;
			double xr = xm + p1;
			double c = 0.134 + 20.5/(15.3+m);
			double a = (fm-xl)/(fm-xl*p);
			double b = (xr-fm)/(xr*q);
			double lambdal = a*(1.0+0.5*a);
			double lambdar = b*(1.0+0.5*b);
			double p2 = p1*(1+2*c);
			double p3 = p2 + c/lambdal;
			double p4 = p3 + c/lambdar;
			while (true)
			{ /* Step 1 */
				int y;
				int k;
				double u = rnd.nextDouble();
				double v = rnd.nextDouble();
				u *= p4;
				if (u <= p1) return (int)(xm-p1*v+u);
				/* Step 2 */
				if (u > p2)
				{ /* Step 3 */
					if (u > p3)
					{ /* Step 4 */
						y = (int)(xr-Math.log(v)/lambdar);
						if (y > n) continue;
						/* Go to step 5 */
						v = v*(u-p3)*lambdar;
					}
					else
					{
						y = (int)(xl+Math.log(v)/lambdal);
						if (y < 0) continue;
						/* Go to step 5 */
						v = v*(u-p2)*lambdal;
					}
				}
				else
				{
					double x = xl + (u-p1)/c;
					v = v*c + 1.0 - Math.abs(m-x+0.5)/p1;
					if (v > 1) continue;
					/* Go to step 5 */
					y = (int)x;
				}
				/* Step 5 */
				/* Step 5.0 */
				k = Math.abs(y-m);
				if (k > 20 && k < 0.5*n*p*q-1.0)
				{ /* Step 5.2 */
					double rho = (k/(n*p*q))*((k*(k/3.0 + 0.625) + 0.1666666666666)/(n*p*q)+0.5);
					double t = -k*k/(2*n*p*q);
					double A = Math.log(v);
					if (A < t-rho) return y;
					else if (A > t+rho) continue;
					else
					{ /* Step 5.3 */
						double x1 = y+1;
						double f1 = m+1;
						double z = n+1-m;
						double w = n-y+1;
						double x2 = x1*x1;
						double f2 = f1*f1;
						double z2 = z*z;
						double w2 = w*w;
						if (A > xm * Math.log(f1/x1) + (n-m+0.5)*Math.log(z/w)
						      + (y-m)*Math.log(w*p/(x1*q))
						      + (13860.-(462.-(132.-(99.-140./f2)/f2)/f2)/f2)/f1/166320.
						      + (13860.-(462.-(132.-(99.-140./z2)/z2)/z2)/z2)/z/166320.
						      + (13860.-(462.-(132.-(99.-140./x2)/x2)/x2)/x2)/x1/166320.
						      + (13860.-(462.-(132.-(99.-140./w2)/w2)/w2)/w2)/w/166320.)
							continue;
						return y;
					}
				}
				else
				{ /* Step 5.1 */
					int i;
					double s = p/q;
					double aa = s*(n+1);
					double f = 1.0;
					for (i = m; i < y; f *= (aa/(++i)-s));
					for (i = y; i < m; f /= (aa/(++i)-s));
					if (v > f) continue;
					return y;
				}
			}
		}
	}

	public static double[] nextVectorUnnormalizedProbs(RandomGenerator rnd, double[] unnormalizedProbs, int numDraws){
		double[] vec = new double[unnormalizedProbs.length];
		for (int d=0; d<numDraws; d++){
			vec[nextIntUnnormalizedProbs(rnd, unnormalizedProbs)] += 1;
		}
		return vec;
	}
	
	
	/**
	 * Returns a random integer with the probabilities specified by unnormalized probabilities.
	 * Uses a binary search for additional speed-ups.
	 * 
	 * @throws IllegalArgumentException if any of the weights are negative
	 */
	public static int nextIntUnnormalizedProbs(RandomGenerator rnd, double[] unnormalizedProbs) {
		double[] cumUnnormalizedProbs = new double[unnormalizedProbs.length];
		double cum = 0.0;
		for (int i = 0; i < unnormalizedProbs.length; i++) {
			if (unnormalizedProbs[i] < 0.0) {
				throw new IllegalArgumentException("weights must be non-negative");
			}
			cum += unnormalizedProbs[i];
			cumUnnormalizedProbs[i] = cum;
		}
		
		double u = rnd.nextDouble() * DoubleArrays.last(cumUnnormalizedProbs);
		
		if (cumUnnormalizedProbs.length < BINARY_SEARCH_THRESHOLD) {
			return linearSearch(cumUnnormalizedProbs, u);
		} else {
			return binarySearch(cumUnnormalizedProbs, u);
		}
	}
	
	private static int linearSearch(double[] cumUnnormalizedProbs, double u) {
		for (int i = 0; i < cumUnnormalizedProbs.length; i++) {
			if (u < cumUnnormalizedProbs[i]) {
				return i;
			}
		}

		throw new IllegalStateException("Problem with sampling");
	}

	private static int binarySearch(double[] cumUnnormalizedProbs, double u) {
		int lowerBracket = 0;
		int upperBracket = cumUnnormalizedProbs.length - 1;
	  int middleIndex = 0;
		while (lowerBracket <= upperBracket) {
			middleIndex = (lowerBracket + upperBracket) >>> 1;
			double middleValue = cumUnnormalizedProbs[middleIndex];
			if (middleValue > u) {
				upperBracket = middleIndex - 1;
			} else if (middleValue < u) {
				// We adjust middleIndex directly for the (likely) case when we fall through
				lowerBracket = ++middleIndex;
			} else {
				return middleIndex;
			}
		}
		return middleIndex;
	}

	/**
	 * Returns a random integer with the probabilities specified by unnormalized log probabilities.
	 * 
	 * This sampler is based on the observation that log(\sum_i e^{x_i}) = 
	 * log(x_1 * \sum_{i=2} e^{x_i - x_1}) which in turn is equivalent to 
	 * x_1 + log(1 + \sum_{i=2} e^{x_i - x_1}). In other words, sampling requires computing the logSum
	 * in order to normalize, but if we cache the cumulative sum of the exponents, we only perform as
	 * many log operations as is necessary for search. Since we compute the highest probable element
	 * first, this can result in significant reductions in cost. Furthermore, we use a binary search,
	 * further reducing the cost.
	 */
	public static int nextIntUnnormalizedLogProbs(RandomGenerator rnd, double[] unnormalizedLogProbs) {
	  double[] copy = unnormalizedLogProbs.clone();
	  DoubleArrays.logNormalizeToSelf(copy);
	  DoubleArrays.expToSelf(copy);
	  double u1 = rnd.nextDouble();
	  double acc = 0.0;
	  for (int i = 0; i < copy.length; i++) {
	    acc += copy[i];
	    if (acc > u1) {
	      return i;
	    }
	  }
	  System.out.println(Arrays.toString(unnormalizedLogProbs));
      System.out.println(Arrays.toString(copy));
	  throw new IllegalStateException("this should never happen");
	  
	  // FIXME(rhaertel): there appears to be a bug; for now, I'm skipping over these optimizations.
	  //
//		// Finding the max is helpful for two reasons: (1) we can avoid exps with parameters that are
//	  // many orders of magnitude smaller. (2) being the most probable, we can speed up sampling by
//	  // checking it first
//		int argMax = DoubleArrays.argMax(unnormalizedLogProbs);
//		double max = unnormalizedLogProbs[argMax];
//		
//		// We move the max (most probable) to the beginning to make sampling faster
//		DoubleArrays.swap(unnormalizedLogProbs, 0, argMax);
//
//		// This will hold the cumulative sum of exponentiated differences
//		double[] cumSum = cumSumOfExponentiatedDiffs(unnormalizedLogProbs, max);
//		
//		// Undo the previous swap to leave x unchanged
//		DoubleArrays.swap(unnormalizedLogProbs, 0, argMax);
//		
//		double r = rnd.nextDouble();
//		double u = Math.log(r) + Math.log(1.0 + cumSum[cumSum.length - 1]);
//		// Was the most probable item randomly chosen? We check first since it is most probable. This
//		// is particularly advantageous in spiky distributions.
//		//
//		// Note the mathematical equivalence with r < p(X = 0), r ~ Unif(0,1)
//		//   log(r) < log p(X = 0)
//		//   log(r) < log exp(x[0]) - log(\sum_{i=0} exp(x[i]))
//		//   log(r) + log(\sum_{i=1} exp(x_i)) < x[0]
//		//   log(r) + x[0] + log(1 + \sum_{i=1} exp(x[i] - x[0])) < x[0]
//		//   log(r) + log(1 + \sum_{i=1} exp(x[i] - x[0])) < 0
//		//   u + log(1 + \sum_{i=1} exp(x[i] - x[0])) < 0
//		if (u < 0) {
//			return argMax;
//		}
//		
//		if (unnormalizedLogProbs.length < BINARY_SEARCH_THRESHOLD) {
//			return linearSearchLogs(argMax, cumSum, u);
//		} else {
//			// Since we only do logs as needed, a binary search can reduce the number of logs
//			return binarySearchLogs(argMax, cumSum, 1, unnormalizedLogProbs.length - 1, u);
//		}
	}

	private static double[] cumSumOfExponentiatedDiffs(double[] x, double max) {
		double[] cumSum = new double[x.length];
		
		double sumExponentiatedDiffs = 0.0;
		for (int i = 1; i < x.length; i++) {
			double diff = x[i] - max;
			if (diff > LOGSUM_THRESHOLD) {
				sumExponentiatedDiffs += Math.exp(diff);
			}
			cumSum[i] = sumExponentiatedDiffs; 
		}
		return cumSum;
	}

	private static int linearSearchLogs(int argMax, double[] cumSum, double u) {
		for (int i = 1; i < cumSum.length; i++) {
			if (u < Math.log(1.0 + cumSum[i])) {
				return returnVal(i, argMax);
			}
		}

		throw new IllegalStateException("Problem with log sampling");
	}

	private static int binarySearchLogs(int argMax, double[] cum, int lowerBracket, int upperBracket, double u) {
		int middleIndex = 0;
		while (lowerBracket <= lowerBracket) {
			middleIndex = (lowerBracket + upperBracket) >>> 1;
			double middleValue = Math.log(1.0 + cum[middleIndex]);
			if (middleValue > u) {
				upperBracket = middleIndex - 1;
			} else if (middleValue < u) {
				// We adjust middleIndex directly for the (likely) case when we fall through
				lowerBracket = ++middleIndex;
			} else {
				return returnVal(middleIndex, argMax);
			}
		}
		return returnVal(middleIndex, argMax);
	}

	/** Adjusts for the fact that we moved argMax to the beginning **/
	private static int returnVal(int index, int argMax) {
		if (index == argMax) {
			return 0;
		} else if (index <= argMax) {
			return index - 1;
		} else {
			return index;
		}
	}
	
	/**
	 * Returns an array with the specified size where each element 
	 * has been assigned an integer value between 0 (inclusive) and 
	 * max (exclusive) with uniform random probability for each 
	 * independent assignment. 
	 */
	public static int[] nextUniformIndependentIntArray(int size, int max, RandomGenerator rnd){
		int[] result = new int[size];
		for (int i=0; i<size; i++){
			result[i] = rnd.nextInt(max);
		}
		return result;
	}

	public static<T> T sample(Iterable<T> coll, RandomGenerator rnd){
		return sample(Lists.newArrayList(coll), rnd);
	}
	
	public static<T> T sample(List<T> list, RandomGenerator rnd){
		Preconditions.checkNotNull(list);
		Preconditions.checkNotNull(rnd);
		return list.get(rnd.nextInt(list.size()));
	}
	
}
