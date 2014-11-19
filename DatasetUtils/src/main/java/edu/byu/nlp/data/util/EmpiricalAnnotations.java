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
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import edu.byu.nlp.data.FlatInstance;
import edu.byu.nlp.data.types.Dataset;
import edu.byu.nlp.data.types.DatasetInstance;
import edu.byu.nlp.data.types.SparseFeatureVector;

/**
 * @author pfelt
 *
 */
public class EmpiricalAnnotations<L,D> {

	// TODO: decide on best representation given api changes
//  public static EmpiricalAnnotations<SparseFeatureVector, Integer> stripAnnotations(Iterable<FlatInstance<SparseFeatureVector, Integer>> instances){
//    Map<SparseFeatureVector,Multimap<Long, TimedAnnotation<Integer>>> annotations = Maps.newIdentityHashMap();
//    Set<Long> annotators = Sets.newHashSet();
//    
//    for (DatasetInstance inst: dataset){
//      // accumulate a set of annotators
//      annotators.addAll(inst.getAnnotations().keySet());
//      // record copy of annotations
//      annotations.put(inst.getData(), HashMultimap.create(inst.getAnnotations()));
//      // strip annotations
//      inst.getAnnotations().clear();
//    }
//    
//    Integer nullLabel = dataset.getLabelIndex().indexOf(null);
//    
//    return new EmpiricalAnnotations<SparseFeatureVector, Integer>(annotations, annotators, nullLabel);
//  }
//
//  
//  
//  private Map<D,Multimap<Long, TimedAnnotation<L>>> dataAnnotationMap;
//  private Set<Long> annotators;
//  private L nullLabel;
//  
//  public EmpiricalAnnotations(Map<D,Multimap<Long, TimedAnnotation<L>>> dataAnnotationMap,
//       Set<Long> annotators, L nullLabel){
//    this.dataAnnotationMap=dataAnnotationMap;
//    this.annotators=annotators;
//    this.nullLabel=nullLabel;
//  }
//  
//  public Multimap<Long, TimedAnnotation<L>> getAnnotationsFor(D data){
//    if (dataAnnotationMap.containsKey(data)){
//      return dataAnnotationMap.get(data);
//    }
//    return HashMultimap.create();
//  }
//  
//  public long getNumAnnotators(){
//    for (int i=0; i<annotators.size(); i++){
//      if (!annotators.contains((long)i)){
//        throw new RuntimeException();
//      }
//    }
//    return annotators.size();
//  }
//  
//  public L getNullLabel(){
//    return nullLabel;
//  }
  
}
