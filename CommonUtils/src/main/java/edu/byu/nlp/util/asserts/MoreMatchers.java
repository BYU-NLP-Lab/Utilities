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
package edu.byu.nlp.util.asserts;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;

import com.google.common.collect.Lists;

/**
 * @author rah67
 *
 */
public class MoreMatchers {
	
	private MoreMatchers() { }
	
	public static <E extends Comparable<? super E>> Matcher<Collection<? extends E>> hasSameElementsAs(final Collection<? extends E> expected) {
		return new TypeSafeMatcher<Collection<? extends E>>() {

			@Override
			public void describeTo(Description desc) {
				desc.appendValue(expected);
			}

			@Override
			public boolean matchesSafely(Collection<? extends E> actual) {
				List<E> sortedExpected = Lists.newArrayList(expected);
				Collections.sort(sortedExpected);
				List<E> sortedActual = Lists.newArrayList(actual);
				Collections.sort(sortedActual);

				return sortedExpected.equals(sortedActual);
			}
		};
	}
}
