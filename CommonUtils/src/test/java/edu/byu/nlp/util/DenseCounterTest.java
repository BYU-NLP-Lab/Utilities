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

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.AbstractMap;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * @author rah67
 *
 */
public class DenseCounterTest {

	/**
	 * Test method for {@link edu.byu.nlp.util.DenseCounter#getCount(java.lang.Object)}.
	 */
	@Test
	public void testCounter() {
		Counter<Integer> counter = new DenseCounter(10);
        assertEquals(0, counter.getCount(0));
        assertEquals(0, counter.getCount(5));
		assertEquals(0, counter.getCount(1));

		counter.incrementCount(5, 1);
        assertThat(counter.getCount(5)).isEqualTo(1);

		counter.incrementCount(5, 1);
        assertThat(counter.getCount(5)).isEqualTo(2);

		counter.incrementCount(1, 1);
        assertThat(counter.getCount(1)).isEqualTo(1);
		
		counter.decrementCount(5, 1);
		assertThat(counter.getCount(5)).isEqualTo(1);
		
		assertThat(counter.totalCount()).isEqualTo(2);
		
		// The hash code of SimpleImmutableEntry and DenseCounter.Entry is different, so we cannot use a HashSet
		Set<Entry<Integer, Integer>> expected = Sets.newCopyOnWriteArraySet();
		expected.add(new AbstractMap.SimpleImmutableEntry<Integer, Integer>(5, 1));
        expected.add(new AbstractMap.SimpleImmutableEntry<Integer, Integer>(1, 1));
        
        // Any entries in one but not the other must have zero count.
        Set<Entry<Integer, Integer>> diff = Sets.difference(counter.entrySet(), expected);
        for (Entry<Integer, Integer> entry : diff) {
            assertThat(entry.getValue()).isEqualTo(0);
        }
        diff = Sets.difference(expected, counter.entrySet());
        for (Entry<Integer, Integer> entry : diff) {
            assertThat(entry.getValue()).isEqualTo(0);
        }
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
    assertEquals(counter.argMaxList(100, null), Lists.newArrayList("c","a","b","d"));
    assertEquals(counter.argMaxList(0, null), Lists.newArrayList("c","a","b","d"));
    assertEquals(counter.argMaxList(-1, null), Lists.newArrayList("c","a","b","d"));
  }
}
