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
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import edu.byu.nlp.util.PorterStemmerUtil;

/**
 * @author pfelt
 *
 */
public class PorterStemmer implements Function<List<String>, List<String>>, Serializable{
  private static final long serialVersionUID = 1L;
  
  private PorterStemmerUtil stemmer = new PorterStemmerUtil();

  @Override
  public List<String> apply(List<String> words) {
    
    List<String> stemmed = Lists.newArrayList();
    
    for (String word: words){
      char[] wrdArr = word.toCharArray();
      stemmer.add(wrdArr, wrdArr.length);
      stemmer.stem();
      stemmed.add(stemmer.toString());
    }
    
    return stemmed;
    
  }

}
