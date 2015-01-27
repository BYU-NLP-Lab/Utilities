package edu.byu.nlp.math.optimize;

import java.util.Arrays;
import java.util.logging.Logger;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.MultivariateOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizer;

import com.google.common.collect.Sets;

import edu.byu.nlp.util.DoubleArrays;

/**
 * 
 * @author plf1
 *
 * A convenience wrapper on top of a couple of apache optimizers
 *
 */
public class MultivariateOptimizers {
    private static final Logger logger = Logger.getLogger(MultivariateOptimizers.class.getName());
	
	public static enum OptimizationMethod {NONE,GRID,BOBYQA};

	public static PointValuePair optimize(OptimizationMethod optMethod, int maxEvaluations, double[] startPoint, double[][] boundaries, MultivariateFunction func){
		
		// pre-calculation
//		int maxEvaluations = 50;
//		if (args.length>=2){
//			maxEvaluations = Integer.parseInt(args[1]);
//		}
//    double zero = 0.01;
//    double one = 1.-zero;
//    double[] startPoint = {CrowdsourcingLearningCurve.bTheta, CrowdsourcingLearningCurve.bGamma, CrowdsourcingLearningCurve.cGamma};
//    double[][] boundaries = {{zero,zero,zero},{2,one,20}};
//    HyperparamOpt optMethod = (args.length==0)? HyperparamOpt.BOBYQA: HyperparamOpt.valueOf(args[0]);

		final int dims = startPoint.length; 
		MultivariateOptimizer optimizer;
		switch (optMethod){
		case BOBYQA:
	    // see advice here http://commons.apache.org/proper/commons-math/apidocs/org/apache/commons/math3/optim/nonlinear/scalar/noderiv/BOBYQAOptimizer.html
			int numberOfInterpolationPoints = dims + 2; // recommended by docs
			optimizer = new BOBYQAOptimizer(numberOfInterpolationPoints); 
			break;
		case GRID:
			optimizer = new GridOptimizer(
					Sets.newHashSet(zero, 0.1, 0.25, 0.5), // bTheta 
					Sets.newHashSet(zero, 0.5, 0.9), // bGamma
					Sets.newHashSet(0.1, 2., 10.)); // cGamma 
			break; 
		case NONE:
			optimizer = new GridOptimizer( // null optimizer does no work
					Sets.newHashSet(bTheta), // bTheta 
					Sets.newHashSet(bGamma), // bGamma
					Sets.newHashSet(cGamma)); // cGamma 
			break;
		default:
			throw new IllegalArgumentException("unknown hyperparameter optimization method: "+args[0]);
		}
		    
		return optimize(optimizer, startPoint, boundaries, maxEvaluations, func);
	}
	
	public static PointValuePair optimize(final MultivariateOptimizer optimizer, double[] startPoint, double[][] boundaries, int maxEvaluations, MultivariateFunction func){

		final int dims = startPoint.length; 
		final double[] bestPoint = new double[dims+1]; // track best point and value (in case the optimization crashes)
		System.arraycopy(startPoint, 0, bestPoint, 0, dims); // assume the start point if nothing else is found (immediate crash)
		
		try{
			return optimizer.optimize(
	           new ObjectiveFunction(func),
	           new MaxEval(maxEvaluations),
	           GoalType.MAXIMIZE,
	           new InitialGuess(startPoint),
	           new SimpleBounds(boundaries[0],
	                            boundaries[1]));
		}
		catch (Exception e){
			// this usually happens because of 
			// 1) max iterations exceeded 
			// 2) weird bobyqa error that I can't find info about 
			// in either case, accept the best answer so far 
			e.printStackTrace();
			logger.info("Hyperparameter optimizer failed for some reason (too many iterations?). Accepting the best evaluated point so far: "+DoubleArrays.toString(bestPoint));
			return new PointValuePair(Arrays.copyOfRange(bestPoint, 0, dims), bestPoint[dims]);
		}
		
	}
	
}
