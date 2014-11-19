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
public abstract class AbstractStatelessPipeFactory<IL, ID, OL, OD> implements PipeFactory<IL, ID, OL, OD> {

	@Override
	public PipeAndLabeledInstances<IL, ID, OL, OD> processLabeledInstances(
			Iterable<FlatInstance<IL, ID>> input) {
		LabeledInstancePipe<IL, ID, OL, OD> pipe = createLabeledInstancePipe();
		return PipeAndLabeledInstances.from(pipe.apply(input), pipe);
	}
	

	protected abstract LabeledInstancePipe<IL, ID, OL, OD> createLabeledInstancePipe();
	
}
