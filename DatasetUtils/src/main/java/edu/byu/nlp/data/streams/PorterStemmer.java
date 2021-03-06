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
package edu.byu.nlp.data.streams;

import java.io.Serializable;

import com.google.common.base.Function;

import edu.byu.nlp.util.PorterStemmerUtil;

/**
 * @author pfelt
 *
 */
public class PorterStemmer implements Function<String, String>, Serializable {
  private static final long serialVersionUID = 1L;

  private PorterStemmerUtil stemmer = new PorterStemmerUtil();

  @Override
  public String apply(String word) {
    if (word==null){
      return null;
    }

    char[] wrdArr = word.toCharArray();
    stemmer.add(wrdArr, wrdArr.length);
    stemmer.stem();
    return stemmer.toString();

  }

}
