package edu.byu.nlp.data.pipes;

import java.util.Iterator;
import java.util.List;

import org.fest.assertions.Assertions;
import org.junit.Test;

public class RegexpTokenizerTest {

	@Test
	public void testTokenizer(){
		List<String> tokens = new RegexpTokenizer("[A-Za-z]+").apply("hi hello, you :)");
		System.out.println(tokens);
		Iterator<String> itr = tokens.iterator();
		Assertions.assertThat(itr.next()).isEqualTo("hi");
		Assertions.assertThat(itr.next()).isEqualTo("hello");
		Assertions.assertThat(itr.next()).isEqualTo("you");
		Assertions.assertThat(itr.hasNext()).isFalse();
	}
	
}
