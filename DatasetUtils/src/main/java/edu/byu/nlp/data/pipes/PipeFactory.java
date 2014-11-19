/**
 * 
 */
package edu.byu.nlp.data.pipes;

import edu.byu.nlp.data.FlatInstance;

/**
 * @author robbie
 * @author pfelt
 *
 */
public interface PipeFactory<IL, ID, OL, OD> {
	PipeAndLabeledInstances<IL, ID, OL, OD> processLabeledInstances(Iterable<FlatInstance<IL, ID>> input);
}
