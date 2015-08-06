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

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

/**
 * A generic-typed pair of objects.
 * 
 * @author Dan Klein
 */
public class Pair<F, S> implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	F first;
	S second;

	public F getFirst() {
		return first;
	}

	public S getSecond() {
		return second;
	}

	public void setFirst(F pFirst) {
		first = pFirst;
	}

	public void setSecond(S pSecond) {
		second = pSecond;
	}

	public void setBoth(F first, S second) {
		this.first = first;
		this.second = second;
	}

	/**
	 * Creates a pair. Convenient factory method that helps reduce the number of
	 * generics needed in many cases.
	 */
	public static <F, S> Pair<F, S> of(F first, S second) {
		return new Pair<F, S>(first, second);
	}

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof Pair))
      return false;

    @SuppressWarnings("rawtypes")
    final Pair other = (Pair) o;

    return Objects.equal(this.getFirst(), other.getFirst())
        && Objects.equal(this.getSecond(), other.getSecond());
  }

	@Override
	public int hashCode() {
    return Objects.hashCode(getFirst(), getSecond());
	}

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(Pair.class)
      .add("first", getFirst())
      .add("second", getSecond())
      .toString();
  }
  
	public Pair(F first, S second) {
		this.first = first;
		this.second = second;
	}
}
