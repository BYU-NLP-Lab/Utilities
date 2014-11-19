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
package edu.byu.nlp.math.optimize;

public class ValueAndObject<T> {
	private final double value;
	private final T object;
	
	public ValueAndObject(double value, T object) {
		this.value = value;
		this.object = object;
	}

	public double getValue() {
		return value;
	}

	public T getObject() {
		return object;
	}
}