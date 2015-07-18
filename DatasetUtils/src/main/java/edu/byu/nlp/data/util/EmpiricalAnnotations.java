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
package edu.byu.nlp.data.util;

import java.util.Map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import edu.byu.nlp.data.types.DataStreamInstance;
import edu.byu.nlp.data.types.Dataset;
import edu.byu.nlp.data.types.DatasetInfo;
import edu.byu.nlp.data.types.DatasetInstance;
import edu.byu.nlp.data.types.SparseFeatureVector;

/**
 * @author pfelt
 *
 */
public class EmpiricalAnnotations<D,L> {
  
  private Map<String, Multimap<Long, Map<String, Object>>> annotations;
  private DatasetInfo info;
  
  public EmpiricalAnnotations(Map<String, Multimap<Long, Map<String, Object>>> annotations,
       DatasetInfo info){
    this.annotations=annotations;
    this.info=info;
  }
  
  public Multimap<Long, Map<String, Object>> getAnnotationsFor(String source, D data){
    if (annotations.containsKey(source)){
      return annotations.get(source);
    }
    return HashMultimap.create();
  }
  
  public DatasetInfo getDataInfo(){
    return info;
  }
  
  

  /**
   * Create a collection of Empirical annotations of <SparseFeatureVector, Integer> 
   * indexed by <instanceIndex,annotatorIndex>  
   */
  public static EmpiricalAnnotations<SparseFeatureVector, Integer> fromDataset(Dataset dataset){
	Map<String, Multimap<Long, Map<String, Object>>> annotations = Maps.newHashMap();
    
    for (DatasetInstance inst: dataset){
    
      // make sure an annotation multimap exists, if this instance has any annotations
    	String source = inst.getInfo().getSource();
      if (inst.getInfo().getNumAnnotations()>0 && !annotations.containsKey(source)){
        annotations.put(source, HashMultimap.<Long, Map<String, Object>>create());
      }
    	
      // add all annotations to the table, indexed by annotator
      for (Map<String, Object> ann: inst.getAnnotations().getRawAnnotations()){
        Integer annotator = DataStreamInstance.getAnnotator(ann, dataset.getInfo().getIndexers());
        annotations.get(source).put((long)annotator, ann);
      }
    	
    }
    
    return new EmpiricalAnnotations<SparseFeatureVector, Integer>(annotations, dataset.getInfo());
  }

}
