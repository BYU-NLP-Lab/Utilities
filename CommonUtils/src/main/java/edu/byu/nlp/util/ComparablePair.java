/**
 * Copyright 2011 Brigham Young University
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

import java.io.Serializable;

import com.google.common.collect.ComparisonChain;

/**
 * A generic-typed pair of objects that are compared 
 * on the basis of the first, then the second consistuent.
 * 
 * @author plf1
 */
public class ComparablePair<F extends Comparable<F>, S extends Comparable<S>> extends Pair<F,S> implements Serializable, Comparable<ComparablePair<F, S>> {
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a pair. Convenient factory method that helps reduce the number of
	 * generics needed in many cases.
	 */
	public static <F extends Comparable<F>, S extends Comparable<S>> ComparablePair<F, S> of(F first, S second) {
		return new ComparablePair<F, S>(first, second);
	}
  
	public ComparablePair(F first, S second) {
	  super(first, second);
	}

  @Override
  public int compareTo(ComparablePair<F, S> o) {
    return ComparisonChain.start()
        .compare(getFirst(), o.getFirst())
        .compare(getSecond(), o.getSecond())
        .result();
  }
  
}
