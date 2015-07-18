/**
 * 
 */
package edu.byu.nlp.data.streams;

import java.io.Serializable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Function;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableList;

/**
 * @author robbie
 *
 */
// TODO(rhaertel) : define Tokenizer as Function<CharSequence, Iterator<String>> 
// and a separate Function<Iterator<T>, List<T>> or perhaps Collection<T>
public class RegexpTokenizer implements Function<String, List<String>>, Serializable {

	private static final long serialVersionUID = 1L;

	private final Pattern pattern;
	
	public RegexpTokenizer(String regex) {
		this(Pattern.compile(regex));
	}
	
	public RegexpTokenizer(Pattern pattern) {
		this.pattern = pattern;
	}

	// TODO(rhaertel) : this seems more broadly useful; move to a utility class?
	private class TokenIterator extends AbstractIterator<String> {

		private final Matcher matcher;
		
		public TokenIterator(CharSequence stringToTokenize) {
			this.matcher = pattern.matcher(stringToTokenize);
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String computeNext() {
			if (!matcher.find()) {
				return endOfData();
			}
			return matcher.group(0);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> apply(String text) {
		return ImmutableList.copyOf(new TokenIterator(text));
	}

}
