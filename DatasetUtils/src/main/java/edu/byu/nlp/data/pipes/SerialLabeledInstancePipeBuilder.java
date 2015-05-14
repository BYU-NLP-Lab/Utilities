/**
 * 
 */
package edu.byu.nlp.data.pipes;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  private static Logger logger = LoggerFactory.getLogger(SerialLabeledInstancePipeBuilder.class);

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
	  if (f==null){
	    logger.warn("ignoring null data transform");
	    return (SerialLabeledInstancePipeBuilder<ID, IL, TD, OL>) this;
	  }
		pipes.add(Pipes.<OD, TD, OL>labeledInstanceDataTransformingPipe(f));
		return (SerialLabeledInstancePipeBuilder<ID, IL, TD, OL>) this;
	}

	public <TL> SerialLabeledInstancePipeBuilder<ID, IL, OD, TL> addLabelTransform(Function<OL, TL> f) {
    if (f==null){
      logger.warn("ignoring null label transform");
      return (SerialLabeledInstancePipeBuilder<ID, IL, OD, TL>) this;
    }
		pipes.add(Pipes.<OD, OL, TL> labeledInstanceLabelTransformingPipe(f));
		return (SerialLabeledInstancePipeBuilder<ID, IL, OD, TL>) this;
	}

	public SerialLabeledInstancePipeBuilder<ID, IL, OD, OL> addSourceTransform(Function<String, String> f) {
    if (f==null){
      logger.warn("ignoring null source transform");
      return this;
    }
		pipes.add(Pipes.<OD, OL>labeledInstanceSourceTransformingPipe(f));
		return (SerialLabeledInstancePipeBuilder<ID, IL, OD, OL>) this;
	}

	public SerialLabeledInstancePipeBuilder<ID, IL, OD, OL> addAnnotatorIdTransform(Function<Long, Long> f) {
    if (f==null){
      logger.warn("ignoring annotator id transform");
      return this;
    }
		pipes.add(Pipes.<OD, OL>labeledInstanceAnnotatorIdTransformingPipe(f));
		return (SerialLabeledInstancePipeBuilder<ID, IL, OD, OL>) this;
	}

	public SerialLabeledInstancePipeBuilder<ID, IL, OD, OL> addInstanceIdTransform(Function<Long, Long> f) {
    if (f==null){
      logger.warn("ignoring instance id transform");
      return this;
    }
		pipes.add(Pipes.<OD, OL>labeledInstanceInstanceIdTransformingPipe(f));
		return (SerialLabeledInstancePipeBuilder<ID, IL, OD, OL>) this;
	}
}
