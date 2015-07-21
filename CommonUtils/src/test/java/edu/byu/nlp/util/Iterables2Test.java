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

import java.util.ArrayList;
import java.util.List;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * @author plf1
 *
 */
public class Iterables2Test {

  @Test
  public void testFlatten(){
    Iterable<Iterable<Integer>> list = Lists.newArrayList();
    ((ArrayList<Iterable<Integer>>) list).add(Lists.newArrayList(1,2,3));
    ((ArrayList<Iterable<Integer>>) list).add(Lists.newArrayList(4,5,6));
    ((ArrayList<Iterable<Integer>>) list).add(Lists.newArrayList(7,8,9));
    
    List<Integer> flat = Lists.newArrayList(Iterables2.flatten(list));
    Assertions.assertThat(flat).isEqualTo(Lists.newArrayList(1,2,3,4,5,6,7,8,9));
  }
  
  public void testTransform(){
    Iterable<Iterable<Integer>> list = Lists.newArrayList();
    ((ArrayList<Iterable<Integer>>) list).add(Lists.newArrayList(1,2,3));
    ((ArrayList<Iterable<Integer>>) list).add(Lists.newArrayList(4,5,6));
    ((ArrayList<Iterable<Integer>>) list).add(Lists.newArrayList(7,8,9));
    
    List<Iterable<String>> xformed = Lists.newArrayList(Iterables2.transformIterables(list, new Function<Integer, String>() {
      @Override
      public String apply(Integer input) {
        return ""+input;
      }
    }));

    Iterables.elementsEqual(xformed.get(0), Lists.newArrayList("1","2","3"));
    Iterables.elementsEqual(xformed.get(1), Lists.newArrayList("1","2","3"));
    Iterables.elementsEqual(xformed.get(2), Lists.newArrayList("1","2","3"));
    
    
  }
	
} 
