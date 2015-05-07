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
package edu.byu.nlp.data.pipes;

import java.io.Serializable;

import com.google.common.base.Function;

/**
 * @author pfelt
 *
 */
public class ShortWordFilter implements Function<String, String>, Serializable {
  private static final long serialVersionUID = 1L;
  private int tooShortLength;

  public ShortWordFilter(int tooShortLength) {
    this.tooShortLength = tooShortLength;
  }

  @Override
  public String apply(String word) {
    // remove single-letter words (lots of contractions get broken up)
    return (word.length() > tooShortLength)? word: null;
  }
}
