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

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertArrayEquals;

import java.util.Set;

import org.apache.commons.math3.random.MersenneTwister;
import org.junit.Test;

import com.google.common.collect.Sets;

/**
 * @author rah67
 *
 */
public class IntArraysTest {

  @Test
  public void testToString(){
    int[] arr = new int[] {1,2,3};
    assertThat(IntArrays.toString(arr)).isEqualTo("[1,2,3]");
  }

  @Test
  public void testParseIntArray(){
    int[] arr = IntArrays.parseIntArray("[1,2,3]");
    assertArrayEquals(new int[]{1,2,3}, arr);

    arr = IntArrays.parseIntArray("1,2,3");
    assertArrayEquals(new int[]{1,2,3}, arr);
  }
  
  @Test
  public void testSequence(){
    int[] s = IntArrays.sequence(0, 3);
    assertArrayEquals(new int[]{0,1,2}, s);

    s = IntArrays.sequence(8, 10);
    assertArrayEquals(new int[]{8,9}, s);

    s = IntArrays.sequence(5, 6);
    assertArrayEquals(new int[]{5}, s);

    s = IntArrays.sequence(5, 5);
    assertArrayEquals(new int[]{}, s);
    
    try {
      IntArrays.sequence(5, 3);
      assertThat(false).isTrue();
    } catch (Exception e){}

    try {
      IntArrays.sequence(-1, 3);
      assertThat(false).isTrue();
    } catch (Exception e){}
  }
  
  @Test
  public void testShuffled(){
    int[] s = IntArrays.shuffled(IntArrays.sequence(0, 3), new MersenneTwister());
    Set<Integer> answers = Sets.newHashSet(0,1,2);
    assertThat(s.length).isEqualTo(3);
    assertThat(answers.contains(s[0])).isTrue();
    assertThat(answers.contains(s[1])).isTrue();
    assertThat(answers.contains(s[2])).isTrue();
    assertThat(s[0]!=s[1] && s[1]!=s[2] && s[2]!=s[0]).isTrue();
  }
  
  
}
