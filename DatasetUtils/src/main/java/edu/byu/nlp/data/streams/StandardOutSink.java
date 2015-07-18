/**
 * 
 */
package edu.byu.nlp.data.streams;

import java.util.Map;


/**
 * @author robbie
 * @author plf1
 *
 */
public class StandardOutSink<D, L> implements DataStreamSink<Void> {

	@Override
  public Void process(Iterable<Map<String, Object>> data) {
    for (Map<String,Object> label : data) {
			System.out.println(label);
		}
		return null;
	}

}
