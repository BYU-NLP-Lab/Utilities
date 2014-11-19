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

import com.google.common.base.Preconditions;

/**
 * @author rah67
 *
 */
public class TimedEvent {

	private final long startTimeNanos;
	private final long endTimeNanos;
	
	public TimedEvent(long startTimeNanos, long endTimeNanos) {
		Preconditions.checkArgument(startTimeNanos <= endTimeNanos);
		this.startTimeNanos = startTimeNanos;
		this.endTimeNanos = endTimeNanos;
	}

	public final long getStartTimeNanos() {
		return startTimeNanos;
	}
	
	public final long getEndTimeNanos() {
		return endTimeNanos;
	}
	
	public final long getDurationNanos() {
		return endTimeNanos - startTimeNanos;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ (int) (endTimeNanos ^ (endTimeNanos >>> 32));
		result = prime * result
				+ (int) (startTimeNanos ^ (startTimeNanos >>> 32));
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TimedEvent other = (TimedEvent) obj;
		if (endTimeNanos != other.endTimeNanos)
			return false;
		if (startTimeNanos != other.startTimeNanos)
			return false;
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "TimedEvent [startTimeNanoSecs=" + startTimeNanos
				+ ", endTimeNanoSecs=" + endTimeNanos + "]";
	}

	/**
	 * A TimedEvent that starts and ends at zero. 
	 */
  public static TimedEvent Zeros() {
    return new TimedEvent(0, 0);
  }

}
