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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import edu.byu.nlp.test.MoreAsserts;

/**
 * @author rah67
 *
 */
public class HeapsTest {
	
	private List<Integer> expectedTopN(List<Integer> list, int n) {
		List<Integer> expected = Lists.newArrayList(list);
		Collections.sort(expected, new Comparator<Integer>() {
			
			@Override
			public int compare(Integer i1, Integer i2) {
				return i2 - i1;
			}
		});
		expected = expected.subList(0, n);
		Collections.reverse(expected);
		return expected;
	}
	
	/**
	 * Test method for {@link edu.byu.nlp.util.Heaps#largestN(java.lang.Iterable, int, boolean)}.
	 * 
	 * Exhaustively tests all possible values for N for all possible permutations of a small list that
	 * includes repeats.
	 */
	@Test
	public void testTopN() {
		final List<Integer> list = ImmutableList.of(5, 1, 3, 7, 3, 4, 10, 0, 1);
		for (int n = 0; n < list.size(); n++) {
			List<Integer> expected = expectedTopN(list, n);
			for (List<Integer> permutation : Collections2.permutations(list)) {
				List<Integer> unsortedTopN = Heaps.largestN(permutation, n, false);
				MoreAsserts.assertEqualsUnordered(expected, unsortedTopN);
				
				List<Integer> sortedTopN = Heaps.largestN(permutation, n, true);
				assertEquals(expected, sortedTopN);
			}
		}
	}
	
}
