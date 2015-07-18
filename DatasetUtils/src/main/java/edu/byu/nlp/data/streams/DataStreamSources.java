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
package edu.byu.nlp.data.streams;

import java.util.Collections;
import java.util.Map;



/**
 * @author rah67
 * @author plf1
 *
 */
public class DataStreamSources {

	private DataStreamSources() { }
	
	

  /**
   * Create a simple data source that contains a single items:
   * (probably a filesystem path to a file that indexes a 
   * corpus that is being used to initialize a pipe). 
   */
	public static <D,L> DataStreamSource singletonSource(final String source, final Map<String, Object> item){
	  return new DataStreamSource() {
      @Override
      public String getStreamSource() {
        return source;
      }
      @Override
      public Iterable<Map<String, Object>> getStream() {
        return Collections.singletonList(item);
      }
    };
	}
	
}
