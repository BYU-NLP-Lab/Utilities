package edu.byu.nlp.stats;

import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

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
  
  private Map<Integer,Double> summands = Maps.newHashMap();
  private Set<Integer> inactive = Sets.newHashSet();
  private double sum = 0.0;
  
  public void setSummand(int index, double value){
    Preconditions.checkArgument(index>=0,"indexes must be non-negative (not "+index+")");
    
    // remove (if necessary) the old value for this entry
    if (summands.containsKey(index)){
      if (!inactive.contains(index)){
        // subtract previous value from sum
        sum -= summands.get(index);
      }
      // remove 0 values (to maintain sparsity)
      if (value==0.0){
        summands.remove(index);
      }
    }
    
    // avoid adding 0 values (to maintain sparsity)
    if (value!=0.0){
      summands.put(index, value);
      if (!inactive.contains(index)){
        // add to sum
        sum += value;
      }
    }    
    
  }

  /**
   * Summands remember their values, but 
   * don't participate in the total sum while they 
   * are marked inactive. By default, all summands are 
   * active. 
   */
  public void setSummandActive(int i, boolean visible) {
    boolean previouslyVisible = !inactive.contains(i);
    // no change. do nothing
    if (visible==previouslyVisible){
      return;
    }
    
    // becoming active (participating in the sum again)
    else if (visible){
      inactive.remove(i);
      if (summands.containsKey(i)){
        sum += summands.get(i);
      }
    }

    // becoming inactive (removed from the sum)
    else{
      inactive.add(i);
      if (summands.containsKey(i)){
        sum -= summands.get(i);
      }
    }
  }
  
  public double getSum(){
    return sum;
  }

  public MutableSum copy() {
	MutableSum copy = new MutableSum();
	copy.summands = Maps.newHashMap(summands);
	copy.sum = sum;
	return copy;
  }

  public double getSummand(int i) {
    if (inactive.contains(i) || !summands.containsKey(i)){
      return 0.0;
    }
    return summands.get(i);
  }


}
