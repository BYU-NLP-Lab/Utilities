/**
 * Copyright 2012 Brigham Young University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.byu.nlp.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.common.collect.Lists;

/**
 * @author rah67
 *
 */
public class IdentityCounterTest {

	/**
	 * Test method for {@link edu.byu.nlp.util.IdentityCounter#getCount(java.lang.Object)}.
	 */
	@Test
	public void testGetCount() {
		IdentityCounter<String> counter = new IdentityCounter<String>();
		assertEquals(0, counter.getCount(""));
		assertEquals(0, counter.getCount("test"));

		StringBuilder sb = new StringBuilder("test");
		String sameRef1 = sb.toString();
		String sameRef2 = sameRef1;
		String diffRef = sb.toString();
		
		counter.incrementCount(sameRef1, 1);
		assertEquals(1, counter.getCount(sameRef1));
		assertEquals(1, counter.getCount(sameRef2));
		assertEquals(0, counter.getCount(diffRef));

		counter.incrementCount(sameRef2, 1);
		assertEquals(2, counter.getCount(sameRef1));
		assertEquals(2, counter.getCount(sameRef2));
		assertEquals(0, counter.getCount(diffRef));

		counter.incrementCount(diffRef, 1);
		assertEquals(2, counter.getCount(sameRef1));
		assertEquals(2, counter.getCount(sameRef2));
		assertEquals(1, counter.getCount(diffRef));
	}

  @Test
  public void testArgmax(){
    HashCounter<String> counter = new HashCounter<String>();
    counter.incrementCount("a", 3);
    counter.incrementCount("c", 4);
    counter.incrementCount("d", 1);
    counter.incrementCount("b", 2);
    
    assertEquals(counter.argMax(), "c");
    assertEquals(counter.argMaxList(3, null), Lists.newArrayList("c","a","b"));
  }
}
