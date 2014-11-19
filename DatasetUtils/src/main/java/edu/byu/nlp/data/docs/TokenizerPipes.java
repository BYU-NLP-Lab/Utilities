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
package edu.byu.nlp.data.docs;

import java.util.List;

import edu.byu.nlp.data.pipes.Downcase;
import edu.byu.nlp.data.pipes.LabeledInstancePipe;
import edu.byu.nlp.data.pipes.RegexpTokenizer;
import edu.byu.nlp.data.pipes.SerialLabeledInstancePipeBuilder;
import edu.byu.nlp.data.pipes.StopWordRemover;

/**
 * @author rah67
 * 
 */
public class TokenizerPipes {
  private TokenizerPipes() {}

  public static LabeledInstancePipe<String, String, List<String>, String> McCallumAndNigam() {
    return new SerialLabeledInstancePipeBuilder<String, String, String, String>()
        .addDataTransform(new Downcase())
        .<List<String>>addDataTransform(new RegexpTokenizer("[a-zA-Z]+"))
        .addDataTransform(StopWordRemover.malletStopWords()).build();
  }
}
