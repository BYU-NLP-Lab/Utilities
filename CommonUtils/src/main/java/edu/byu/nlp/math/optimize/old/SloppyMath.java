package edu.byu.nlp.math.optimize.old;

import static java.lang.Math.log;


/**
 * Routines for some approximate math functions.
 *
 * @author Dan Klein
 * @author Teg Grenager
 */
public class SloppyMath {

	public static double min(int x, int y) {
        if (x > y)
            return y;
        return x;
    }

    public static double max(int x, int y) {
        if (x > y)
            return x;
        return y;
    }

    public static double abs(double x) {
        if (x > 0)
            return x;
        return -1.0 * x;
    }

    public static double add(double X, double Y) {
        return logAdd(Math.log(X), Math.log(Y));
    }

    public static double logAdd(double logX, double logY) {
        // make a the max
        if (logY > logX) {
            double temp = logX;
            logX = logY;
            logY = temp;
        }
        // now a is bigger
        if (logX == Double.NEGATIVE_INFINITY) {
            return logX;
        }
        double negDiff = logY - logX;
        if (negDiff < -50) {
            return logX;
        }
        return logX + java.lang.Math.log(1.0 + java.lang.Math.exp(negDiff));
    }

    /**
     * logX >= logY
     * 
     * @author rah67
     */
    public static double logSub(double logX, double logY) {
        // make a the max
        if (logY > logX) {
        	throw new IllegalArgumentException("Cannot take the log of a negative number");
        }
        
        // now logX is bigger
        if (logX == Double.NEGATIVE_INFINITY) {
            return logX;
        }
        
        double negDiff = logY - logX;
        if (negDiff < -50) {
            return logX;
        }
        return logX + java.lang.Math.log(1.0 - java.lang.Math.exp(negDiff));
    }
    
    public static double logAdd(double[] logV) {
    	return logAdd(logV, true);
    }
    
    public static double logAdd(double[] logV, boolean findMax) {
        double max = Double.NEGATIVE_INFINITY;
        int maxIndex = 0;
        if (findMax) {
	        for (int i = 0; i < logV.length; i++) {
	            if (logV[i] > max) {
	                max = logV[i];
	                maxIndex = i;
	            }
	        }
        } else {
        	for (int i = 0; i < logV.length; i++) {
        		if (logV[i] > Double.NEGATIVE_INFINITY) {
        			max = logV[i];
        			maxIndex = i;
        			break;
        		}
        	}
        }
        
        if (max == Double.NEGATIVE_INFINITY)
            return Double.NEGATIVE_INFINITY;
        // compute the negative difference
        double threshold = max - 20;
        double sumNegativeDifferences = 0.0;
        for (int i = 0; i < logV.length; i++) {
            if (i != maxIndex && logV[i] > threshold) {
                sumNegativeDifferences += Math.exp(logV[i] - max);
            }
        }
        if (sumNegativeDifferences > 0.0) {
            return max + Math.log1p(sumNegativeDifferences);
        } else {
            return max;
        }
    }

    public static class LogAddCDF {
    	public final double logSum;
    	public final int maxIndex;
    	public final double max;
    	public final double[] table;
		
    	public LogAddCDF(double logSum, int maxIndex, double max, double[] table) {
			this.logSum = logSum;
			this.maxIndex = maxIndex;
			this.max = max;
			this.table = table;
		}
    }
    
    /**
     * Creates a CDF based on an unnormalized vector of probabilities in log space.
     * 
     * @param logV
     * @param findMax	whether or not to search for the actual max
     * @return
     */
    public static LogAddCDF logAddCDF(double[] logV, boolean findMax) {
        double max = Double.NEGATIVE_INFINITY;
        int maxIndex = 0;
        if (findMax) {
	        for (int i = 0; i < logV.length; i++) {
	            if (logV[i] > max) {
	                max = logV[i];
	                maxIndex = i;
	            }
	        }
        } else {
        	for (int i = 0; i < logV.length; i++) {
        		if (logV[i] > Double.NEGATIVE_INFINITY) {
        			max = logV[i];
        			maxIndex = i;
        			break;
        		}
        	}
        }
        if (max == Double.NEGATIVE_INFINITY)
            return null;
        
        // compute the negative difference
        double threshold = max - 20;
        double[] cache = new double[logV.length];
        double cum = cache[0] = 1.;
        for (int i = 0, j = 0; i < logV.length; i++) {
            if (i != maxIndex) {
            	if (logV[i] > threshold) {
	            	final double expDiff = Math.exp(logV[i] - max);
	            	cum += expDiff;
                }
            	++j;
            }
        	cache[j] = cum;
        }
        cum -= 1.0;
        if (cum > 0.0) {
        	final double logSum = max + Math.log1p(cum);
        	return new LogAddCDF(logSum, maxIndex, max, cache);
        } else {
        	return new LogAddCDF(max, maxIndex, max, cache);
        }
    }

    public static double exp(double logX) {
        // if x is very near one, use the linear approximation
        if (abs(logX) < 0.001)
            return 1 + logX;
        return Math.exp(logX);
    }

    /**
     * Taylor approximation of first derivative of the log gamma function
     * 
     * Borrowed from lda-j which was borrowed from lda-c
     * 
     */
    public static double digamma(double x) {
        double p;
        assert x > 0;
        x = x + 6;
        p = 1 / (x * x);
        p = (((0.004166666666667 * p - 0.003968253986254) * p + 0.008333333333333)
            * p - 0.083333333333333)
            * p;
        p = p + log(x) - 0.5 / x - 1 / (x - 1) - 1 / (x - 2) - 1 / (x - 3) - 1
            / (x - 4) - 1 / (x - 5) - 1 / (x - 6);
        return p;
    }

}
