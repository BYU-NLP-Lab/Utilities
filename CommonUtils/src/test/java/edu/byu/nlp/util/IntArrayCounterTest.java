/**
 * Copyright 2014 Brigham Young University
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

import org.fest.assertions.Assertions;
import org.junit.Before;
import org.junit.Test;

/**
 * @author pfelt
 */
public class IntArrayCounterTest {

  private IntArrayCounter c;
  
  @Before
  public void setup(){
    int size = 5;
    int numCategories = 3;
    c = new IntArrayCounter(size, numCategories);

    c.increment(0, 1);
    c.increment(0, 2);
    c.increment(0, 2);

    c.increment(1, 0);
    c.increment(1, 1);
    c.increment(1, 0);

    c.increment(2, 0);

    c.increment(4, 2);
    c.increment(4, 2);
    c.increment(4, 2);
    c.increment(4, 1);
    c.increment(4, 0);
  }
  
  @Test
  public void testValues() {
    Assertions.assertThat(c.values(0)).contains(0,1,2);
    Assertions.assertThat(c.values(1)).contains(2,1,0);
    Assertions.assertThat(c.values(2)).contains(1,0,0);
    Assertions.assertThat(c.values(3)).contains(0,0,0);
    Assertions.assertThat(c.values(4)).contains(1,1,3);
  }
  
  @Test
  public void testArgmax() {
    Assertions.assertThat(c.argmax(0)).isEqualTo(2);
    Assertions.assertThat(c.argmax(1)).isEqualTo(0);
    Assertions.assertThat(c.argmax(2)).isEqualTo(0);
    Assertions.assertThat(c.argmax(3)).isEqualTo(-1);
    Assertions.assertThat(c.argmax(4)).isEqualTo(2);
  }
  
  @Test
  public void testArgmaxes() {
    Assertions.assertThat(c.argmax()).contains(2,0,-1,0,2);
  }
}
