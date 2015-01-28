package edu.byu.nlp.math.optimize;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.MultivariateOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.byu.nlp.util.DoubleArrays;

/**
 * 
 * @author plf1
 *
 * A convenience wrapper on top of a couple of apache optimizers
 *
 */
public class MultivariateOptimizers {
	private static final Logger logger = LoggerFactory.getLogger(MultivariateOptimizers.class);
	
	public static enum OptimizationMethod {NONE,GRID,BOBYQA};

	public static PointValuePair optimize(OptimizationMethod optMethod, int maxEvaluations, double[] startPoint, double[][] boundaries, 
			List<Set<Double>> grid, MultivariateFunction func){
		final int dims = startPoint.length; 

		MultivariateOptimizer optimizer;
		switch (optMethod){
		case BOBYQA:
			// see advice here http://commons.apache.org/proper/commons-math/apidocs/org/apache/commons/math3/optim/nonlinear/scalar/noderiv/BOBYQAOptimizer.html
			int numberOfInterpolationPoints = dims + 2; // recommended by docs
			optimizer = new BOBYQAOptimizer(numberOfInterpolationPoints); 
			break;
		case GRID:
			optimizer = new GridOptimizer(grid);  
			break; 
		case NONE:
			optimizer = new GridOptimizer((Set<Double>[])null);  // returns start values
			break;
		default:
			throw new IllegalArgumentException("unknown hyperparameter optimization method: "+optMethod);
		}

		return optimize(optimizer, startPoint, boundaries, maxEvaluations, func);
	}
	
	public static PointValuePair optimize(final MultivariateOptimizer optimizer, double[] startPoint, double[][] boundaries, 
			int maxEvaluations, final MultivariateFunction func){

		// track the best point evaled so far to return (in case optimization crashes)
		final int dims = startPoint.length; 
		final double[] bestPoint = new double[dims+1]; 
		System.arraycopy(startPoint, 0, bestPoint, 0, dims); // initialize best point with the start point 
		
		PointValuePair optimum;
		try{
			optimum = optimizer.optimize(
	           new ObjectiveFunction(new MultivariateFunction(){
		            @Override
		            public double value(double[] point) {
		              double val = func.value(point);
		              if (val>bestPoint[dims]){
			              System.arraycopy(point, 0, bestPoint, 0, dims); // copy point
			              bestPoint[dims] = val; // copy val
		              }
		              return val;
		            }
		           }),
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
			logger.info("Hyperparameter optimizer failed for some reason (too many iterations?). "
					+ "Accepting the best evaluated point so far (last element is objective value): "+DoubleArrays.toString(bestPoint));
			optimum = new PointValuePair(Arrays.copyOfRange(bestPoint, 0, dims), bestPoint[dims]);
		}

	    logger.info("finished hyperparameter optimization in "+optimizer.getEvaluations()+" evaluations");
	    return optimum;
	}
	
}
