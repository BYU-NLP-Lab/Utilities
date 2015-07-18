package edu.byu.nlp.data.streams;

import java.io.Serializable;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import edu.byu.nlp.util.Pair;

/**
 * 
 * @author plf1
 *
 * Punctuation gets stripped from documents. But emoticons 
 * should usually not be stripped (they do not necessarily play 
 * to the role of punctuation). This function transforms common 
 * emoticons into text tokens so they are not stripped later.
 *
 */
public class EmoticonTransformer implements Function<String, String>, Serializable {
	private static final long serialVersionUID = 1L;

	public static final String SMILEY_TOKEN = "esmiley";
	public static final String FROWNY_TOKEN = "efrowny";

	public static final String REGEX_SMILEY_MOUTH = "[\\)\\]D>]";
	public static final String REGEX_FROWNY_MOUTH = "[\\(\\[<]";
	public static final String REGEX_EYES = "[:=8]";
	public static final String REGEX_NOSE = "[\\p{Punct}co0-9]*";

	@SuppressWarnings("unchecked")
	private static List<Pair<String,String>> emoticonMapping = Lists.newArrayList(
			Pair.of(REGEX_EYES+REGEX_NOSE+REGEX_SMILEY_MOUTH, SMILEY_TOKEN)
			,Pair.of(REGEX_EYES+REGEX_NOSE+REGEX_FROWNY_MOUTH, FROWNY_TOKEN)
			); 

	@Override
	public String apply(String txt) {
		for (Pair<String,String> emoticonMap: emoticonMapping){
			txt = txt.replaceAll(emoticonMap.getFirst(), emoticonMap.getSecond());
		}
		return txt;
	}
	
}
