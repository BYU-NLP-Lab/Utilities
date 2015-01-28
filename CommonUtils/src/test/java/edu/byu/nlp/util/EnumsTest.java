package edu.byu.nlp.util;

import org.fest.assertions.Assertions;
import org.junit.Test;

public class EnumsTest {

	private enum LAB {A,B,C};
	@Test
	public void test() {
		Assertions.assertThat(Enums.isEnumValue("D", LAB.class)).isFalse();
		Assertions.assertThat(Enums.isEnumValue("a", LAB.class)).isTrue();
		Assertions.assertThat(Enums.isEnumValue("B", LAB.class)).isTrue();
		Assertions.assertThat(Enums.isEnumValue("c", LAB.class)).isTrue();
		Assertions.assertThat(Enums.isEnumValue("C", LAB.class)).isTrue();
		Assertions.assertThat(Enums.isEnumValue("Charlie", LAB.class)).isFalse();
		Assertions.assertThat(Enums.isEnumValue("98jfelkap9zids", LAB.class)).isFalse();
	}
	
}
