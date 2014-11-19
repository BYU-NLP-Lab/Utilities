/**
 * 
 */
package edu.byu.nlp.math.optimize.old;

import java.util.EventListener;

/**
 * @author rah67
 *
 */
public interface IterationListener extends EventListener {
	void initialValues(IterationEvent e);
	void endOfIteration(EndOfIterationEvent e);
	void endingValues(IterationEvent e);
}
