/**
 * 
 */
package edu.byu.nlp.math.optimize.old;


/**
 * @author rah67
 *
 */
public class EndOfIterationEvent extends IterationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final double[] startingParameters;
	private final double startingValue;
	
	public EndOfIterationEvent(final Object source, final int iteration, final double[] startingParameters, final double startingObjectiveValue, final double[] parameters, final double objectiveValue, final long startTime, final long endTime) {
		super(source, iteration, parameters, objectiveValue, startTime, endTime);
		this.startingParameters = startingParameters;
		this.startingValue = startingObjectiveValue;
	}

	/**
	 * @return the initialParameters
	 */
	public double[] getStartingParameters() {
		return startingParameters;
	}

	public double getStartingValue() {
		return startingValue;
	}
	
}
