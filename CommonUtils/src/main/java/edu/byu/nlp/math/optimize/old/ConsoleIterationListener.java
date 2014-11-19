/**
 * 
 */
package edu.byu.nlp.math.optimize.old;

import java.io.PrintStream;

/**
 * @author rah67
 *
 */
public class ConsoleIterationListener implements IterationListener {

	private static final double MILLISECONDS_PER_SECOND =  1000;
	private static final double MILLISECONDS_PER_MINUTE = 60 * MILLISECONDS_PER_SECOND;
	private static final double MILLISECONDS_PER_HOUR = 60 * MILLISECONDS_PER_MINUTE;
	private static final double MILLISECONDS_PER_DAY = 24 * MILLISECONDS_PER_HOUR;
	private static final int NUM_TIME_DIGITS = 2;
	
	private final PrintStream out;

	public ConsoleIterationListener() {
		this(System.out);
	}
	
	public ConsoleIterationListener(PrintStream out) {
		this.out = out;
	}

	/* (non-Javadoc)
	 * @see edu.berkeley.nlp.math.IterationListener#initialValues(edu.berkeley.nlp.math.IterationEvent)
	 */
	public void initialValues(IterationEvent e) {
		out.printf(" Starting value = %.6f (%s)\n", e.getObjectiveValue(), msToUnit(e.getEventDuration()));
		out.flush();
	}

	/* (non-Javadoc)
	 * @see edu.berkeley.nlp.math.IterationListener#endOfIteration(edu.berkeley.nlp.math.EndOfIterationEvent)
	 */
	public void endOfIteration(EndOfIterationEvent e) {
		out.printf("  Iteration %d ended with value %.6f (%s)\n",e.getIteration(), e.getObjectiveValue(), msToUnit(e.getEventDuration()));				
		out.flush();
	}

	/* (non-Javadoc)
	 * @see edu.berkeley.nlp.math.IterationListener#endingValues(edu.berkeley.nlp.math.IterationEvent)
	 */
	public void endingValues(IterationEvent e) {
		out.printf(" Ending value = %.6f in %d iterations (%s)\n", e.getObjectiveValue(), e.getIteration(), msToUnit(e.getEventDuration()));
		out.flush();
	}

	// TODO : somehow make the code in Timer for pretty printing strings reusable
	private String msToUnit(long milliseconds) {
		String unit;
		double value = milliseconds;
		
		if (milliseconds > MILLISECONDS_PER_DAY) {
			value = milliseconds / MILLISECONDS_PER_DAY; 
			unit = " day";
		} else if (milliseconds > MILLISECONDS_PER_HOUR) {
			value = milliseconds / MILLISECONDS_PER_HOUR;
			unit = " hr";
		} else if (milliseconds > MILLISECONDS_PER_MINUTE) {
			value = milliseconds / MILLISECONDS_PER_MINUTE;
			unit = " min";
		} else if (milliseconds > MILLISECONDS_PER_SECOND) {
			value = milliseconds / MILLISECONDS_PER_SECOND;
			unit = " sec";
		} else { 
			unit = " ms";
		}
		
//		if (value != milliseconds && Math.round(value * Math.pow(10, NUM_TIME_DIGITS)) == Math.pow(10, NUM_TIME_DIGITS)) {
		if (value != milliseconds && value != 1.0) {
			unit += "s";
		}
		return String.format("%." + NUM_TIME_DIGITS + "f %s", value, unit);
	}

}
