/**
 * 
 */
package edu.byu.nlp.data.streams;

import java.io.Serializable;
import java.util.Locale;

import com.google.common.base.Function;

/**
 * @author robbie
 *
 */
public class Downcase implements Function<String, String>, Serializable {

	private static final long serialVersionUID = 1L;

	private final Locale locale;
	
	public Downcase() {
		this(Locale.getDefault());
	}
	
	public Downcase(Locale locale) {
		this.locale = locale;
	}
	
	@Override
	public String apply(String str) {
		return str.toLowerCase(locale);
	}

}
