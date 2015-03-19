package edu.byu.nlp.util;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.google.common.base.Function;

public class Functions2Test {

	@Test
	public void testCompose() {

		Function<String, String> composedFunction = Functions2.compose(new Function<String, String>() {
			@Override
			public String apply(String input) {
				return input+"a";
			}
		}, new Function<String, String>() {
			@Override
			public String apply(String input) {
				return input+"b";
			}
		}, new Function<String, String>() {
			@Override
			public String apply(String input) {
				return input+"c";
			}
		});

		Assertions.assertThat(composedFunction.apply("hi")).isEqualTo("hicba");
		
	}

}
