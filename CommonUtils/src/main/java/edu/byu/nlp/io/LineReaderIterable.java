package edu.byu.nlp.io;

import java.util.Iterator;

/**
 * An {@code Iterable} over lines produced by a {@code Readable}, e.g. a File.
 * 
 * @author rah67
 *
 */
public class LineReaderIterable extends AbstractIterable<String> {

	private final Readable readable;

	/**
	 * @param readable the source of data from which lines will be iterated over
	 */
	public LineReaderIterable(Readable readable) {
		this.readable = readable;
	}

	@Override
	public Iterator<String> iterator() {
		return new LineReaderIterator(readable);
	}

}