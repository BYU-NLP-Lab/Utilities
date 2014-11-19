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
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * A resettable boolean latch. This is similar to CountDownLatch, except it only requires a single signal to fire.
 * In addition, this latch is resettable	.
 * 
 * The code is based on the sample code in the Javadocs for AbstractQueuedSynchronized
 * @author rah67
 *
 */
public class ResettableBooleanLatch {

	private static class Sync extends AbstractQueuedSynchronizer {

		private static final long serialVersionUID = 1L;

		public Sync() {
			setState(0);
		}
		
		public boolean isSignalled() {
			return getState() != 0;
		}

		protected int tryAcquireShared(int ignore) {
			return isSignalled() ? 1 : -1;
		}

		protected boolean tryReleaseShared(int ignore) {
			setState(1);
			return true;
		}
		
		public void reset() {
			setState(0);
		}
	}

	private final Sync sync = new Sync();

	public boolean isSignalled() {
		return sync.isSignalled();
	}

	public void signal() {
		sync.releaseShared(1);
	}

	public void await() throws InterruptedException {
		sync.acquireSharedInterruptibly(1);
	}

	public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
		return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
	}

	public void reset() throws InterruptedException {
		sync.reset();
	}

}
