package edu.byu.nlp.math.optimize;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.fest.assertions.Assertions;
import org.junit.Test;

import com.google.common.collect.Sets;

import edu.byu.nlp.util.Counter;
import edu.byu.nlp.util.DenseCounter;

public class GridOptimizerTest {

	@Test
	public void testMaximize() {
		GridOptimizer optim = new GridOptimizer(
				Sets.newHashSet(1.,2.),
				Sets.newHashSet(3.,4.,5.),
				Sets.newHashSet(6.,7.)
				);
		// num points in grid = 2 x 3 x 2 = 12
		PointValuePair argmax = optim.optimize(
				new ObjectiveFunction(new MultivariateFunction() {
					@Override
					public double value(double[] point) {
						return 10*point[0] - 1*point[1] + 1*point[2];
					}
				}),
				GoalType.MAXIMIZE
		);

		Assertions.assertThat(argmax.getValue()).isEqualTo(10*2 - 3 + 7);
		double[] vars = argmax.getPointRef();
		Assertions.assertThat(vars[0]).isEqualTo(2.);
		Assertions.assertThat(vars[1]).isEqualTo(3.);
		Assertions.assertThat(vars[2]).isEqualTo(7.);
	}

	@Test
	public void testMinimize() {
		GridOptimizer optim = new GridOptimizer(
				Sets.newHashSet(1.,2.),
				Sets.newHashSet(3.,4.,5.),
				Sets.newHashSet(6.,7.)
				);
		// num points in grid = 2 x 3 x 2 = 12
		final Counter<Integer> iterationCounter = new DenseCounter(2);
		PointValuePair argmax = optim.optimize(
				new ObjectiveFunction(new MultivariateFunction() {
					@Override
					public double value(double[] point) {
						iterationCounter.incrementCount(0, 1);
						return 10*point[0] - 1*point[1] + 1*point[2];
					}
				}),
				GoalType.MINIMIZE
		);

//		Assertions.assertThat(iterationCounter.getCount(0)).isEqualTo(optim.getEvaluations()); // getEvaluations doesn't work by default
		Assertions.assertThat(iterationCounter.getCount(0)).isEqualTo(2*3*2);
		Assertions.assertThat(argmax.getValue()).isEqualTo(10*1 - 5 + 6);
		double[] vars = argmax.getPointRef();
		Assertions.assertThat(vars[0]).isEqualTo(1.);
		Assertions.assertThat(vars[1]).isEqualTo(5.);
		Assertions.assertThat(vars[2]).isEqualTo(6.);
	}

	@Test
	public void testNullOptimizer() {
		GridOptimizer optim = new GridOptimizer(
				Sets.newHashSet(2.),
				Sets.newHashSet(4.),
				Sets.newHashSet(7.)
				);
		// num points in grid = 2 x 3 x 2 = 12
		PointValuePair argmax = optim.optimize(
				new ObjectiveFunction(new MultivariateFunction() {
					@Override
					public double value(double[] point) {
						return 10*point[0] - 1*point[1] + 1*point[2];
					}
				}),
				GoalType.MINIMIZE
		);

		Assertions.assertThat(argmax.getValue()).isEqualTo(10*2 - 4 + 7);
		double[] vars = argmax.getPointRef();
		Assertions.assertThat(vars[0]).isEqualTo(2.);
		Assertions.assertThat(vars[1]).isEqualTo(4.);
		Assertions.assertThat(vars[2]).isEqualTo(7.);
	}
	
// // example of built-in optimizer
//	@Test
//	public void testBogus() {
//		BOBYQAOptimizer optim = new BOBYQAOptimizer(7,BOBYQAOptimizer.DEFAULT_INITIAL_RADIUS,1e-2);
//		// num points in grid = 2 x 3 x 2 = 12
//		PointValuePair argmax = optim.optimize(
//				new ObjectiveFunction(new MultivariateFunction() {
//					@Override
//					public double value(double[] point) {
//						return 10*point[0] - 1*point[1] + 1*point[2];
////						return Math.random();
//					}
//				}),
//				GoalType.MAXIMIZE,
//	            new MaxEval(100),
//                new InitialGuess(new double[]{5,5,5}),
//				new SimpleBounds(new double[]{0,0,0}, new double[]{10,10,10})
//		);
//
//		Assertions.assertThat(argmax.getValue()).isEqualTo(10*10 - 0 + 10);
//		double[] vars = argmax.getPointRef();
//		Assertions.assertThat(vars[0]).isEqualTo(10.);
//		Assertions.assertThat(vars[1]).isEqualTo(0.);
//		Assertions.assertThat(vars[2]).isEqualTo(10.);
//	}
}
