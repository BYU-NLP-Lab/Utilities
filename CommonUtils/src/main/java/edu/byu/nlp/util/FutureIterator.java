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
 * <p>An iterator that blocks while waiting for its next item. This pattern is particularly well-suited for
 * applications in which the producer can cheaply produce the next value upon request while the consumer(s) may take
 * much longer to process the result. Under these circumstances, the producer might cause an unbounded queue to run
 * out of memory or may have to block when using a bounded queue.</p>
 * 
 * The null value is used as a sentinel value to determine whether or not iteration is done, since consistency
 * cannot be ensured between calls to hasNext() and next(). Thus, null values are not permitted by this iterator.
 * Example usage: </p>
 *  <pre>
 *    FutureIterator<V> it = lazyProducer.FutureIterator();
 *    V value;
 *    while ( (value = it.next()) != null) {
 *      doSomethingWith(value);
 *    }
 *  </pre>
 * 
 * @author rah67
 */
public interface FutureIterator<V> {
	V next() throws InterruptedException;
	/** Returns null if timeout occurs or if the iterator is empty **/
	V next(long timeout, TimeUnit unit) throws InterruptedException;
}