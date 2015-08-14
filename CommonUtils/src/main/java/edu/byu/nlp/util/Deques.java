package edu.byu.nlp.util;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.random.RandomGenerator;

import com.google.common.collect.Lists;

public class Deques {

  public static <T> Deque<T> randomizedDeque(Collection<T> collection, RandomGenerator rnd){
    List<T> shuffled = Lists.newArrayList(collection);
    Collections.shuffle(shuffled, new Random(rnd.nextLong()));
    return new ArrayDeque<T>(shuffled);
  }
  
}
