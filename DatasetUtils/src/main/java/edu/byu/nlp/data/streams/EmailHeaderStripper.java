/**
 * 
 */
package edu.byu.nlp.data.pipes;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Function;

/**
 * Strips the header from an RFC-822 style message.
 * 
 * @author robbie
 *
 */
public class EmailHeaderStripper implements Function<String, String>, Serializable {
	
	private static final long serialVersionUID = 1L;

	private static final Pattern BLANK_LINE_PATTERN = Pattern.compile("\n\n|\r\r|\n\r\n\r");
  private static final Pattern SUBJECT_LINE_PATTERN = Pattern.compile("^Subject:");
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String apply(String input) {

	  // split the body from the header
    Matcher matcher = BLANK_LINE_PATTERN.matcher(input);
    if (!matcher.find()) {
      return "";
    }
    String header = input.substring(0, matcher.end());
    String body = input.substring(matcher.end(), input.length());
    
    // keep only the subject line from the header
	  String subject = "";
	  for (String line: header.split("\n")){
	    if (SUBJECT_LINE_PATTERN.matcher(line).find()){
	      // strip the "subject" portion
	      subject = line.replaceAll(SUBJECT_LINE_PATTERN.toString(), "");
	    }
	  }
		
		return subject + "\n\n" + body;
	}

}
