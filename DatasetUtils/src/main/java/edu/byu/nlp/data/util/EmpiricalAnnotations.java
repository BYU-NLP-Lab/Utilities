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

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import edu.byu.nlp.data.BasicFlatInstance;
import edu.byu.nlp.data.FlatInstance;
import edu.byu.nlp.data.measurements.ClassificationMeasurements.ClassificationAnnotationMeasurement;
import edu.byu.nlp.data.types.Dataset;
import edu.byu.nlp.data.types.DatasetInfo;
import edu.byu.nlp.data.types.DatasetInstance;
import edu.byu.nlp.data.types.Measurement;
import edu.byu.nlp.data.types.SparseFeatureVector;

/**
 * @author pfelt
 *
 */
public class EmpiricalAnnotations<D,L> {

  private Map<String, Multimap<Integer, FlatInstance<D,L>>> annotations;
  private DatasetInfo info;
  private Collection<FlatInstance<D,L>> measurements;
  private Multimap<Integer, FlatInstance<D,L>> perAnnotatorMeasurements;
  
  public EmpiricalAnnotations(Map<String, Multimap<Integer, FlatInstance<D,L>>> annotations,
       Collection<FlatInstance<D,L>> measurements, DatasetInfo info){
    this.annotations=annotations;
    this.measurements=measurements;
    this.perAnnotatorMeasurements = HashMultimap.create();
    for (FlatInstance<D, L> meas: measurements){
      this.perAnnotatorMeasurements.put(meas.getAnnotator(), meas);
    }
    this.info=info;
  }
  
  public Multimap<Integer, FlatInstance<D,L>> getAnnotationsFor(String source, D data){
    if (source==null){
      // measurement request (not attached to any specific document)
      return getPerAnnotatorMeasurements();
    }
    else if (annotations.containsKey(source)){
      return annotations.get(source);
    }
    return HashMultimap.create();
  }

  public Map<String, Multimap<Integer, FlatInstance<D,L>>> getPerInstancePerAnnotatorAnnotations(){
	  return annotations;
  }
  public Multimap<Integer, FlatInstance<D,L>> getPerAnnotatorMeasurements(){
    return perAnnotatorMeasurements;
  }
  public Collection<FlatInstance<D,L>> getMeasurements(){
    return measurements;
  }
  
  public DatasetInfo getDataInfo(){
    return info;
  }
  
  

  /**
   * Create a collection of Empirical annotations of <SparseFeatureVector, Integer> 
   * indexed by <instanceSrc,annotatorIndex>  
   */
  public static EmpiricalAnnotations<SparseFeatureVector, Integer> fromDataset(Dataset dataset){
	Map<String, Multimap<Integer, FlatInstance<SparseFeatureVector,Integer>>> annotations = Maps.newHashMap();
    
    for (DatasetInstance inst: dataset){
    
      // make sure an annotation multimap exists, if this instance has any annotations
    	String source = inst.getInfo().getRawSource();
      if (inst.getInfo().getNumAnnotations()>0 && !annotations.containsKey(source)){
        annotations.put(source, HashMultimap.<Integer,FlatInstance<SparseFeatureVector,Integer>>create());
      }
    	
      // add all annotations to the table, indexed by annotator
      for (FlatInstance<SparseFeatureVector,Integer> ann: inst.getAnnotations().getRawAnnotations()){
        annotations.get(source).put(ann.getAnnotator(), ann);
      }
    	
    }
    
    // filter all annotation measurements (they are taken care of in the annotations)
    Collection<FlatInstance<SparseFeatureVector, Integer>> measurements = Lists.newArrayList();
    for (Measurement meas: dataset.getMeasurements()){
      if (!(meas instanceof ClassificationAnnotationMeasurement)){
        measurements.add(new BasicFlatInstance<SparseFeatureVector, Integer>(-1, null, meas.getAnnotator(), null, meas, meas.getStartTimestamp(), meas.getEndTimestamp()));
      }
    }
    
    return new EmpiricalAnnotations<SparseFeatureVector, Integer>(annotations, measurements, dataset.getInfo());
  }

}
