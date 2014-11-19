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

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

/**
 * @author rah67
 *
 */
public class Iterators2Test {

	private static class IntegerComparator implements Comparator<Integer> {

		/** {@inheritDoc} */
		@Override
		public int compare(Integer int1, Integer int2) {
			// TODO : handle nulls
			return int1 - int2;
		}
		
	}
	
	/**
	 * Test method for {@link edu.byu.nlp.util.Iterators2#topN(java.util.Iterator, int, java.util.Comparator)}.
	 */
	@Test
	public void testTopN() {
		List<Integer> topN = Iterators2.topN(Iterators.forArray(3,4,2,4,6,7,3,6,1,1), 4, new IntegerComparator());
		assertEquals(topN.size(), 4);
		assertEquals(new Integer(4), topN.get(0));
		assertEquals(new Integer(6), topN.get(1));
		assertEquals(new Integer(6), topN.get(2));
		assertEquals(new Integer(7), topN.get(3));
	}
	
	@Test
	public void testRepeatItems() {
	    Iterator<Integer> it = Iterators2.repeatItems(Iterators.forArray(3, 4, 2, 4, 6, 6, 3), 3);
	    List<Integer> actual = Lists.newArrayList(it);
	    assertThat(actual).isEqualTo(Arrays.asList(3, 3, 3, 4, 4, 4, 2, 2, 2, 4, 4, 4, 6, 6, 6, 6, 6, 6, 3, 3, 3));
	}
	
	@Test
	public void testCycle() {
	    Iterator<Integer> it = Iterators2.cycle(ImmutableList.of(3, 4, 2, 4, 6, 6, 3), 3);
	    List<Integer> actual = Lists.newArrayList(it);
	    assertThat(actual).isEqualTo(Arrays.asList(3, 4, 2, 4, 6, 6, 3,
	                                               3, 4, 2, 4, 6, 6, 3,
	                                               3, 4, 2, 4, 6, 6, 3));
	}

	@Test
	public void testLazyConcat(){
		List<Iterator<Integer>> iterators = Lists.newArrayList();
		iterators.add(Iterators.forArray(1,2,3,4,5));
		iterators.add(Iterators.forArray(6,7,8,9,10));
		iterators.add(Iterators.forArray(11,12,13,14,15));
		List<Integer> actual = Lists.newArrayList(Iterators2.lazyConcatenate(iterators));
		assertThat(actual).isEqualTo(Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15));
	}
	
} 
