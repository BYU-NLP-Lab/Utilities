/**
 * 
 */
package edu.byu.nlp.data.pipes;

import edu.byu.nlp.data.FlatInstance;


/**
 * @author pfelt
 *
 */
public interface LabeledInstancePipe<ID, IL, OD, OL> {
	Iterable<FlatInstance<OD, OL>> apply(Iterable<FlatInstance<ID, IL>> instances);
}
