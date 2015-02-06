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

import java.io.FileNotFoundException;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

/**
 * @author pfelt
 *
 */
public class Multisets2Test {

  public static Multiset<String> stubMultiset(){
    Multiset<String> mset = HashMultiset.create();
    mset.add("a",5);
    mset.add("b",2);
    mset.add("c",7);
    return mset;
  }
  
  @Test
  public void testMinCount() throws FileNotFoundException{
    Assertions.assertThat( Multisets2.minCount(stubMultiset())).isEqualTo(2);
  }

  @Test
  public void testMinElement() throws FileNotFoundException{
    Assertions.assertThat( Multisets2.minElements(stubMultiset())).contains("b");
  }
  
}
