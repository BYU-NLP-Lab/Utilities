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

/**
 * @author rah67
 *
 */
public class Timers {
	
	private Timers() {}
	
	public static interface Timer {
	    Stoppable start();
	}
	
	public static interface Stoppable {
		TimedEvent stop();
	}
	
	private static class SystemStoppable implements Stoppable {

		private final long startTimeNanoSecs;
		
		public SystemStoppable(long startTimeNanoSecs) {
			this.startTimeNanoSecs = startTimeNanoSecs;
		}

		/** {@inheritDoc} */
		@Override
		public TimedEvent stop() {
			long stopTimeNanoSecs = System.nanoTime();
			return new TimedEvent(startTimeNanoSecs, stopTimeNanoSecs);
		}
		
	}
	
	public static Stoppable start() {
		return new SystemStoppable(System.nanoTime());
	}
	
	private static class SystemTimer implements Timer {

        /** {@inheritDoc} */
        @Override
        public Stoppable start() {
            return new SystemStoppable(System.nanoTime());
        }
	    
	}
	
	public static Timer systemTimer() {
	    return new SystemTimer();
	}
	
	private static class StoppableStub implements Stoppable {

	    private final TimedEvent timedEvent;
	    
        public StoppableStub(TimedEvent timedEvent) {
            this.timedEvent = timedEvent;
        }

        /** {@inheritDoc} */
        @Override
        public TimedEvent stop() {
            return timedEvent;
        }
	}
	
	private static class TimerStub implements Timer {

	    private final TimedEvent timedEvent;
	    
        public TimerStub(TimedEvent timedEvent) {
            this.timedEvent = timedEvent;
        }

        /** {@inheritDoc} */
        @Override
        public Stoppable start() {
            return new StoppableStub(timedEvent);
        }
	    
	}
	
	public static Timer stub(long startTimeNanos, long endTimeNanos) {
	    return new TimerStub(new TimedEvent(0, 0));
	}
	
	/**
	 * Returns a timer with 0 duration.
	 */
	public static Timer zeroTimer() {
	    return stub(0, 0);
	}
}
