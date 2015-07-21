package edu.byu.nlp.data.pipes;

import org.fest.assertions.Assertions;
import org.junit.Test;

import edu.byu.nlp.data.streams.EmoticonTransformer;

public class EmoticonTransformerTest {

	static public String[] smileys = new String[]{
		":)",":-)",":<)",":c)",":D",":>","8>","=)","=->","=0)"
	};

	static public String[] frownies = new String[]{
		":(",":-(",":<(",":c(",":[",":<","8<","=(","=-<","=0("
	};
	
	@Test
	public void testIndividual(){
		assertNotSmiley(new EmoticonTransformer().apply("hi"),"hi");
		for (String smiley: smileys){
			assertSmiley(new EmoticonTransformer().apply(smiley),smiley);
		}
		for (String frowny: frownies){
			assertNotSmiley(new EmoticonTransformer().apply(frowny),frowny);
		}
	}

	

//	@Test
//	public void testSmileyCombined(){
//		String transformed = new EmoticonTransformer().apply("hi there, you :) :<) :c) =-) =o) 8) 8-)");
//		System.out.println(transformed);
//		Assertions.assertThat(transformed).isEqualTo("hi there, you smiley smiley smiley smiley smiley smiley smiley");
//	}
	
	

	public static void assertSmiley(String txt, String original){
		Assertions.assertThat(txt).as("original emoticon was "+original).isEqualTo(EmoticonTransformer.SMILEY_TOKEN);
	}
	public static void assertNotSmiley(String txt, String original){
		Assertions.assertThat(txt).as("original emoticon was "+original).isNotEqualTo(EmoticonTransformer.SMILEY_TOKEN);
	}
}
