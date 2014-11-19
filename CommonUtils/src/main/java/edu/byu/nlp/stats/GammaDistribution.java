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

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.random.RandomGenerator;

import com.google.common.base.Preconditions;

/**
 * @author rah67
 *
 */
public class GammaDistribution {

  /**
   * self-contained gamma generator. Multiply result with scale parameter (or
   * divide by rate parameter). After Teh (npbayes).
   * 
   * Taken From knowceans.
   */
  public static double sample(double shape, RandomGenerator rnd) {
    Preconditions.checkArgument(shape > 0.0);
    Preconditions.checkNotNull(rnd);

    if (shape == 1.0) {
      /* Exponential */
      return -Math.log(rnd.nextDouble());
    } else if (shape < 1.0) {
      /* Use Johnk's generator */
      double cc = 1.0 / shape;
      double dd = 1.0 / (1.0 - shape);
      while (true) {
        double xx = Math.pow(rnd.nextDouble(), cc);
        double yy = xx + Math.pow(rnd.nextDouble(), dd);
        if (yy <= 1.0) {
          // FIXME: assertion error for rr = 0.010817814317923407
          // assert yy != 0 && xx / yy > 0 : "rr = " + rr;
          // INFO: this if is a hack
          if (yy != 0 && xx / yy > 0) {
            return -Math.log(rnd.nextDouble()) * xx / yy;
          }
        }
      }
    } else { /* rr > 1.0 */
      /* Use bests algorithm */
      double bb = shape - 1.0;
      double cc = 3.0 * shape - 0.75;
      while (true) {
        double uu = rnd.nextDouble();
        double vv = rnd.nextDouble();
        double ww = uu * (1.0 - uu);
        double yy = Math.sqrt(cc / ww) * (uu - 0.5);
        double xx = bb + yy;
        if (xx >= 0) {
          double zz = 64.0 * ww * ww * ww * vv * vv;
          assert zz > 0 && bb != 0 && xx / bb > 0;
          if ((zz <= (1.0 - 2.0 * yy * yy / xx))
              || (Math.log(zz) <= 2.0 * (bb * Math.log(xx / bb) - yy))) {
            return xx;
          }
        }
      }
    }
  }

  /**
   * Generates gamma samples, one for each element in shapes.
   * 
   * @param shapes
   */
  public static double[] sample(double[] shapes, RandomGenerator rnd) {
    double[] gamma = new double[shapes.length];
    for (int i = 0; i < gamma.length; i++) {
      gamma[i] = sample(shapes[i], rnd);
    }
    return gamma;
  }

  public static void sampleToSelf(double[] shapes, RandomGenerator rnd) {
    for (int i = 0; i < shapes.length; i++) {
      shapes[i] = sample(shapes[i], rnd);
    }
  }

  /**
   * Generates gamma samples, one for each element in shapes.
   * 
   * @param shapes
   */
  public static double[] sample(RealVector shapes, RandomGenerator rnd) {
    double[] gamma = new double[shapes.getDimension()];
    for (int i = 0; i < gamma.length; i++) {
      gamma[i] = sample(shapes.getEntry(i), rnd);
    }
    return gamma;
  }

  /**
   * sample from gamma distribution with defined shape a and scale b:
   * <p>
   * x ~ x^(a-1) * exp(-x/b) / ( gamma(a) * b^a )
   * <p>
   * E(x) = ab, V(x) = (ab)^2. Note that instead of the scale parameter b,
   * often a rate parameter r = 1/b is used: E(x) = a/r, V(x) = (a/r)^2. For
   * sampling, the following are equivalent: Gamma(a,1)*b <=> Gamma(a,b), with
   * shape parametrisation; Gamma(a,1)/r <=> Gamma(a,r) with rate
   * parametrisation.
   * 
   * @param shape
   * @param scale
   * @return
   */
  public static double randGamma(double shape, double scale, RandomGenerator rnd) {
    return sample(shape, rnd) * scale;
  }

  /**
   * Samples a new Gamma distributed random variate for each parameter setting specified as elements
   * of the shapes matrix.
   */
  public static double[][] sample(RealMatrix shapes, RandomGenerator rnd) {
    double[][] gammas = new double[shapes.getRowDimension()][shapes.getColumnDimension()];
    for (int i = 0; i < shapes.getRowDimension(); i++) {
      for (int j = 0; j < shapes.getColumnDimension(); j++) {
        gammas[i][j] = sample(shapes.getEntry(i, j), rnd);
      }
    }
    return gammas;
  }

  /**
   * Samples a new Gamma distributed random variate for each parameter setting specified as elements
   * of the shapes matrix.
   */
  public static double[][] sample(double[][] shapes, RandomGenerator rnd) {
    double[][] gammas = new double[shapes.length][];
    for (int i = 0; i < shapes.length; i++) {
      gammas[i] = sample(shapes[i], rnd);
    }
    return gammas;
  }

}
