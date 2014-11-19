/**
 * 
 */
package edu.byu.nlp.data.pipes;

import edu.byu.nlp.data.FlatInstance;

/**
 * @author robbie
 * @author plf1
 *
 */
public class StandardOutSink<D, L> implements DataSink<D, L, Void> {

	@Override
	public Void processLabeledInstances(Iterable<FlatInstance<D, L>> data) {
		for (FlatInstance<D, L> label : data) {
			System.out.println(label);
		}
		return null;
	}

}
