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

import java.util.List;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.byu.nlp.data.docs.DocPipes;
import edu.byu.nlp.data.pipes.StopWordRemover;

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
    List<String> actual = DocPipes.sentenceTransform(stopWordRemover).apply(Lists.newArrayList("a", "b", "c", "d", "a", "b", "c", "d"));
    Assertions.assertThat(actual).containsExactly("c", "d", "c", "d");
  }
}
