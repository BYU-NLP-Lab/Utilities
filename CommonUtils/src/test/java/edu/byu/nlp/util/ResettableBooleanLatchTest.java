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

import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * @author rah67
 *
 */
public class ResettableBooleanLatchTest {

	private class Producer implements Runnable {
		private final ResettableBooleanLatch latch;
		private volatile boolean done;
		
		public Producer(ResettableBooleanLatch latch) {
			this.latch = latch;
			this.done = false;
		}

		/** {@inheritDoc} */
		@Override
		public void run() {
			while (!done) {
				latch.signal();
			}
		}
		
		public void stop() {
			done = true;
		}
	}
	
	private class Consumer implements Runnable {
		private final Producer producer;
		private final ResettableBooleanLatch latch;
		private final int numRuns;
		
		public Consumer(Producer producer, ResettableBooleanLatch latch, int numRuns) {
			this.producer = producer;
			this.latch = latch;
			this.numRuns = numRuns;
		}

		/** {@inheritDoc} */
		@Override
		public void run() {
			try {
				for (int i = 0; i < numRuns; i++) {
					latch.await();
					latch.reset();
				}
				producer.stop();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Test
	public void raceConditionTest() {
		final int numRuns = 10000000;
		ResettableBooleanLatch latch = new ResettableBooleanLatch();
		Producer producer = new Producer(latch);
		new Thread(new Consumer(producer, latch, numRuns), "Consumer").start();
		Thread producerThread = new Thread(producer, "Producer");
		producerThread.start();
		try {
			producerThread.join();
		} catch (InterruptedException e) {
			fail();
			e.printStackTrace();
		}
	}

}
