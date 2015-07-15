package edu.byu.nlp.stats;

import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

/**
 * @author plf1
 *
 * Maintains a sum along with all of its summands, 
 * so that one can mutate summands and 
 * calculate the updated sum in constant time. 
 * Summands are indexed by integer, but maintained 
 * sparsely. 
 */
public class MutableSum {
  
  Map<Integer,Double> summands = Maps.newHashMap();
  double sum = 0.0;
  
  void setSummand(int index, double value){
    Preconditions.checkArgument(index>=0,"indexes must be non-negative (not "+index+")");
    
    // remove (if necessary) the old value for this entry
    if (summands.containsKey(index)){
      // subtract previous value from sum
      sum -= summands.get(index);
      // remove 0 values (to maintain sparsity)
      if (value==0.0){
        summands.remove(index);
      }
    }
    
    // avoid adding 0 values (to maintain sparsity)
    if (value!=0.0){
      summands.put(index, value);
      // add to sum
      sum += value;
    }    
    
  }
  
  double getSum(){
    return sum;
  }

}
