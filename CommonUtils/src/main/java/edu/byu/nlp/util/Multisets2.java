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

import com.google.common.collect.Multiset;

/**
 * @author pfelt
 *
 */
public class Multisets2 {

  /**
   * Return the element with the smallest count in a multiset, or else null
   * if there are no elements
   */
  public static <T> T minElement(Multiset<T> mset){
    // return the smallest element
    T argmin = null;
    int min = Integer.MAX_VALUE;
    for (com.google.common.collect.Multiset.Entry<T> entry: mset.entrySet()){
      if (entry.getCount()<min){
        argmin = entry.getElement();
        min = entry.getCount();
      }
    }
    return argmin;
  }
  
  /**
   * @return
   */
  public static <T> int minCount(Multiset<T> mset){
    T argmin = minElement(mset);
    if (argmin==null){
      return 0;
    }
    else{
      return mset.count(argmin);
    }
  }
  
}
