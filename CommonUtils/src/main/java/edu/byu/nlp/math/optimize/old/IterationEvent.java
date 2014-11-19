/**
 * 
 */
package edu.byu.nlp.math.optimize.old;

import java.util.EventObject;

/**
 * @author rah67
 *
 */
public class IterationEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final int iteration;
	private final double[] parameters;
	private final double objectiveValue;
	private final long eventStartTime;
	private final long eventEndTime;

	public IterationEvent(final Object source, final int iteration, final double[] parameters, final double objectiveValue, final long eventStartTime, final long eventEndTime) {
		super(source);
		this.iteration = iteration;
		this.parameters = parameters;
		this.objectiveValue = objectiveValue;
		this.eventStartTime = eventStartTime;
		this.eventEndTime = eventEndTime;
	}

	/**
	 * @return the iteration
	 */
	public int getIteration() {
		return iteration;
	}

	/**
	 * @return the parameters
	 */
	public double[] getParameters() {
		return parameters;
	}

	/**
	 * @return the objectiveValue
	 */
	public double getObjectiveValue() {
		return objectiveValue;
	}

	/**
	 * @return the eventEndTime
	 */
	public long getEventEndTime() {
		return eventEndTime;
	}

	/**
	 * @return the eventStartTime
	 */
	public long getEventStartTime() {
		return eventStartTime;
	}

	public long getEventDuration() {
		return eventEndTime - eventStartTime;
	}
}
