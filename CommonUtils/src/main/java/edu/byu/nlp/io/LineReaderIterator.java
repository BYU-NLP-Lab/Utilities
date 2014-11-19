package edu.byu.nlp.io;

import java.io.IOException;

import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractIterator;
import com.google.common.io.LineReader;


/**
 * An {@code Iterator} over lines produced by a {@code Readable}, e.g. a File.
 * 
 * @author rah67
 *
 */
public class LineReaderIterator extends AbstractIterator<String> {

	public final LineReader reader;
	
	/**
	 * @param readable the source of data from which lines will be iterated over
	 */
	public LineReaderIterator(Readable readable) {
		Preconditions.checkNotNull(readable);

		this.reader = new LineReader(readable);
	}

	private String readLineQuietly() {
		try {
			return reader.readLine();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected String computeNext() {
		String line = readLineQuietly();
		return (line == null) ? endOfData() : line;
	}

}