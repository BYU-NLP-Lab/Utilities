/**
 * Copyright 2013 Brigham Young University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.byu.nlp.data.pipes;

import java.util.Map;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.byu.nlp.data.streams.DataStream;
import edu.byu.nlp.data.streams.DataStreams;
import edu.byu.nlp.data.streams.StopWordRemover;
import edu.byu.nlp.data.types.DataStreamInstance;
import edu.byu.nlp.util.Maps2;

/**
 * @author rah67
 * 
 */
public class StopWordRemoverTest {

  /**
   * Test method for {@link edu.byu.nlp.data.pipes.StopWordRemover#apply(java.util.List)}.
   */
  @Test
  public void testApply() {
    StopWordRemover stopWordRemover = new StopWordRemover(Sets.newHashSet("a", "b"));
    @SuppressWarnings("unchecked")
    DataStream actual = DataStream.withSource("test source", Lists.<Map<String,Object>>newArrayList(
        Maps2.<String,Object>hashmapOf("data",Lists.newArrayList("a","b","c","d","a","b","c","d"))))
        
        .transform(DataStreams.Transforms.transformIterableFieldValues("data", stopWordRemover))
        ;
        
    Map<String,Object> item = (Map<String,Object>) Iterables.getOnlyElement(actual);
    @SuppressWarnings("unchecked")
    Iterable<String> data = (Iterable<String>)DataStreamInstance.getData(item);
    
    Assertions.assertThat(Lists.newArrayList(data)).containsExactly("c", "d", "c", "d");
  }
}
