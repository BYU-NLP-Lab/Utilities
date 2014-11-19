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

import java.util.concurrent.TimeUnit;

/**
 * @author rah67
 *
 */
public interface BlockingHeap<V> extends Heap<V> {
	/** Blocking version of offer() */
	V take() throws InterruptedException;
	
	/** Blocking variant of poll() */
	void put(V val) throws InterruptedException;
	
	/** Blocking variant of offerThenPoll() */
	V takeThenPut(V val) throws InterruptedException;

	/** Like take(), but with a timeout; returns null when the call timesout **/
    V poll(long timeout, TimeUnit timeUnit) throws InterruptedException;
	
	// TODO(rah67): implement timeout versions of offer.
}
