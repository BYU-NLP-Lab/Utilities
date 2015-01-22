package edu.byu.nlp.math.optimize;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.OptimizationData;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.MultivariateOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;

import com.google.common.collect.Sets;

import edu.byu.nlp.util.DoubleArrays;

/**
 * An optimizer that tries a grid of parameter possibilities 
 * and returns the one with the highest objective value.
 *
 * Expects sets of values, one per parameter. Takes the 
 * cartesian product of these possiblilities (equivalent to nested 
 * for-loops, or "grid"). 
 */
public class GridOptimizer extends MultivariateOptimizer{
	private static final Logger logger = Logger.getLogger(GridOptimizer.class.getName());

	private MultivariateFunction function;
	private GoalType goal;

	private Set<Double>[] variableSettingSets;

	@SafeVarargs
	public GridOptimizer(final Set<Double> ... variableSettingSets) {
		super(null);
		this.variableSettingSets=variableSettingSets;
	}

	@Override
	protected PointValuePair doOptimize() {
		
		PointValuePair best = new PointValuePair(null, worstValue(goal));
		
		for (List<Double> variableSettingsList: Sets.cartesianProduct(variableSettingSets)){
			double[] variableSettings = DoubleArrays.fromList(variableSettingsList);
			// test next point in grid
			double objectiveValue = function.value(variableSettings);
			PointValuePair current = new PointValuePair(variableSettings, objectiveValue);
			
			// is this the best?
			if (betterThan(objectiveValue,best.getValue(),goal)){
				best = current;
			}
			
		}
		
		// return best so far
        return best; 
	}
	
	private boolean betterThan(double a, double b, GoalType goal){
		return (goal==GoalType.MAXIMIZE)? a>b: a<b;
	}
	private double worstValue(GoalType goal){
		return goal==GoalType.MINIMIZE ? Double.MAX_VALUE : -Double.MAX_VALUE;
	}

    /**
     * Scans the list of (required and optional) optimization data that
     * characterize the problem.
     *
     * @param optData Optimization data.
     * The following data will be looked for:
     * <ul>
     *  <li>{@link ObjectiveFunction}</li>
     *  <li>{@link GoalType}</li>
     * </ul>
     */
    @Override
    protected void parseOptimizationData(OptimizationData... optData) {
        // Allow base class to register its own data.
        super.parseOptimizationData(optData);

        // The existing values (as set by the previous call) are reused if
        // not provided in the argument list.
        for (OptimizationData data : optData) {
            if (data instanceof GoalType) {
                this.goal = (GoalType) data;
                continue;
            }
            if (data instanceof ObjectiveFunction) {
                this.function = ((ObjectiveFunction) data).getObjectiveFunction();
                continue;
            }
        }
    }
    
	
}
