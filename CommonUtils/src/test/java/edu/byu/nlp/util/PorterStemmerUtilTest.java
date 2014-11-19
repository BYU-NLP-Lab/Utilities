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
import org.junit.Test;

/**
 * @author pfelt
 *
 */
public class PorterStemmerUtilTest {

  PorterStemmerUtil stemmer = new PorterStemmerUtil();
  
  @Test
  public void testStemmer(){
    String txt = "The blackened ducking loughed lightly";
    String[] words = txt.split(" ");
    
    for (int i=0; i<words.length; i++){
      char[] wrdArr = words[i].toCharArray();
      stemmer.add(wrdArr, wrdArr.length);
      stemmer.stem();
      words[i] = stemmer.toString();
    }
    
    String result = Strings.join(words, " ");
    Assertions.assertThat(result).isEqualTo("The blacken duck lough lightli");
  }
  
}
