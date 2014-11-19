/**
 * 
 */
package edu.byu.nlp.io;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;

/**
 * Provides {@code toString()}, {@code hashCode()}, and {@code equals()}.
 * 
 * @author rah67
 *
 */
public abstract class AbstractIterable<T> implements Iterable<T> {

	@Override
	public int hashCode() {
		return Objects.hashCode(Iterables.toArray(this, Object.class));
	}

	@SuppressWarnings("rawtypes") // Necessary for cast to Iterable
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Iterable))
			return false;
		
		return Iterables.elementsEqual(this, (Iterable) obj);
	}

	@Override
	public String toString() {
		return Iterables.toString(this);
	}

}
