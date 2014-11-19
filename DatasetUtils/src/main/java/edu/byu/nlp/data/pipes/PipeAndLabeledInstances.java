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
public class PipeAndLabeledInstances<IL, ID, OL, OD> {

	private final Iterable<FlatInstance<OL, OD>> data;
	private final LabeledInstancePipe<IL, ID, OL, OD> pipe;
	
	private PipeAndLabeledInstances(Iterable<FlatInstance<OL, OD>> data, LabeledInstancePipe<IL, ID, OL, OD> pipe) {
		this.data = data;
		this.pipe = pipe;
	}

	public LabeledInstancePipe<IL, ID, OL, OD> getPipe() {
		return pipe;
	}
	
	public Iterable<FlatInstance<OL, OD>> getLabeledInstances() {
		return data;
	}

	public static <IL, ID, OL, OD> PipeAndLabeledInstances<IL, ID, OL, OD> from(Iterable<FlatInstance<OL, OD>> data,
			LabeledInstancePipe<IL, ID, OL, OD> pipe) {

		return new PipeAndLabeledInstances<IL, ID, OL, OD>(data, pipe);
	}
	
}
