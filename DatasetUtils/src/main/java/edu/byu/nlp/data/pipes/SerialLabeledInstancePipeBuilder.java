/**
 * 
 */
package edu.byu.nlp.data.pipes;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * Builds a pipe that process an arbitrary number of pipes serially.
 * This class assists with the generics.
 * 
 * @author robbie
 * @author plf1
 *
 */
@SuppressWarnings("unchecked")
public class SerialLabeledInstancePipeBuilder<ID, IL, OD, OL> {

	@SuppressWarnings("rawtypes")
	private List<LabeledInstancePipe> pipes = Lists.newArrayList();
	
	public SerialLabeledInstancePipeBuilder(){}
	
	public SerialLabeledInstancePipeBuilder<ID, IL, OD, OL> clone(){
	  SerialLabeledInstancePipeBuilder<ID, IL, OD, OL> newBuilder = new SerialLabeledInstancePipeBuilder<>();
	  newBuilder.pipes = Lists.newArrayList(pipes); 
	  return newBuilder;
	}
	
	public <D, L> SerialLabeledInstancePipeBuilder<ID, IL, D, L> add(LabeledInstancePipe<OD, OL, D, L> p) {
		pipes.add(p);
		return (SerialLabeledInstancePipeBuilder<ID, IL, D, L>) this;
	}
	
	public LabeledInstancePipe<ID, IL, OD, OL> build() {
		return new LabeledInstanceSerialPipe<ID, IL, OD, OL>(pipes);
	}

	public <TD> SerialLabeledInstancePipeBuilder<ID, IL, TD, OL> addDataTransform(Function<OD, TD> f) {
		pipes.add(Pipes.<OD, TD, OL>labeledInstanceDataTransformingPipe(f));
		return (SerialLabeledInstancePipeBuilder<ID, IL, TD, OL>) this;
	}

	public <TL> SerialLabeledInstancePipeBuilder<ID, IL, OD, TL> addLabelTransform(Function<OL, TL> f) {
		pipes.add(Pipes.<OD, OL, TL> labeledInstanceLabelTransformingPipe(f));
		return (SerialLabeledInstancePipeBuilder<ID, IL, OD, TL>) this;
	}

	public SerialLabeledInstancePipeBuilder<ID, IL, OD, OL> addSourceTransform(Function<String, String> f) {
		pipes.add(Pipes.<OD, OL>labeledInstanceSourceTransformingPipe(f));
		return (SerialLabeledInstancePipeBuilder<ID, IL, OD, OL>) this;
	}

	public SerialLabeledInstancePipeBuilder<ID, IL, OD, OL> addAnnotatorIdTransform(Function<Long, Long> f) {
		pipes.add(Pipes.<OD, OL>labeledInstanceAnnotatorIdTransformingPipe(f));
		return (SerialLabeledInstancePipeBuilder<ID, IL, OD, OL>) this;
	}

	public SerialLabeledInstancePipeBuilder<ID, IL, OD, OL> addInstanceIdTransform(Function<Long, Long> f) {
		pipes.add(Pipes.<OD, OL>labeledInstanceInstanceIdTransformingPipe(f));
		return (SerialLabeledInstancePipeBuilder<ID, IL, OD, OL>) this;
	}
}
