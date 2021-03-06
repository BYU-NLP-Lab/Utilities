/**
 * Copyright 2015 Brigham Young University
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
package edu.byu.nlp.data.app;

import java.io.IOException;

import org.apache.commons.math3.random.MersenneTwister;
import org.fest.assertions.Assertions;
import org.junit.Test;

import edu.byu.nlp.data.app.AnnotationStream2Annotators.ClusteringMethod;

/**
 * @author plf1
 *
 */
public class AnnotatorParameterEstimatorTest {

  @Test
  public void kmeansTest() throws IOException{
    
    int[][][] confusionMatrices = new int[][][]{
        // strong diagonal
        { {5,0,1},
          {1,5,2},
          {0,1,7}
        },
        { {7,1,0},
          {0,5,0},
          {1,0,6}
        },
        { {6,0,0},
          {1,4,1},
          {0,2,5}
        },
        // strong lower-left
        { {6,1,1},
          {5,4,2},
          {6,3,5}
        },
        { {4,1,1},
          {7,5,2},
          {5,8,8}
        },
        { {2,0,0},
          {8,3,0},
          {5,6,6}
        },
        // strong upper-right
        { {5,5,8},
          {1,3,7},
          {0,1,6}
        },
        { {4,4,4},
          {0,4,6},
          {0,1,9}
        },
        { {8,7,9},
          {0,6,5},
          {1,0,6}
        },
    };
    
    // do clustering
    ClusteringMethod aggregate = ClusteringMethod.KMEANS;
    int k = 3;
    int maxIterations = 1000;
    double smooth = 0.00001;
	double[][][] annotatorParameters = AnnotationStream2Annotators.confusionMatrices2AnnotatorParameters(confusionMatrices);
	final int[] clusterAssignments = AnnotationStream2Annotators.clusterAnnotatorParameters(annotatorParameters, aggregate, k, maxIterations, smooth, new MersenneTwister(1));
	double[][][] clusteredParameters = AnnotationStream2Annotators.aggregateAnnotatorParameterClusters(annotatorParameters, clusterAssignments);

//	System.out.println(IntArrays.toString(clusterAssignments));
	// make sure items {0,1,2}, {3,4,5}, and {6,7,8} cluster together (event though we can't predict indices) 
	Assertions.assertThat(clusterAssignments[0]).isEqualTo(clusterAssignments[1]);
	Assertions.assertThat(clusterAssignments[0]).isEqualTo(clusterAssignments[2]);
	Assertions.assertThat(clusterAssignments[3]).isEqualTo(clusterAssignments[4]);
	Assertions.assertThat(clusterAssignments[3]).isEqualTo(clusterAssignments[5]);
	Assertions.assertThat(clusterAssignments[6]).isEqualTo(clusterAssignments[7]);
	Assertions.assertThat(clusterAssignments[6]).isEqualTo(clusterAssignments[8]);
	
//    // make sure these are in a consistent ordering for testing
//    Arrays.sort(annotatorParameters,new Comparator<double[][]>() {
//      @Override
//      public int compare(double[][] o1, double[][] o2) {
//        double t1 = Matrices.trace(o1);
//        double t2 = Matrices.trace(o2);
//        return Double.compare(t2, t1);
//      }
//    });
	
//    System.out.println(Matrices.toString(clusteredParameters));
    Assertions.assertThat(clusteredParameters.length).isEqualTo(3);
    // one cluster has a strong diagonal
    for (int r=0; r<clusteredParameters.length; r++){
      for (int c=0; c<clusteredParameters[r].length; c++){
        if (r==c){
          assertLargeEntry(clusteredParameters, clusterAssignments[0], r, c);
        }
        else{
          assertSmallEntry(clusteredParameters, clusterAssignments[0], r, c);
        }
      }
    }    
    // one cluster has strong diagonal and lower left    
    for (int r=0; r<clusteredParameters.length; r++){
      for (int c=0; c<clusteredParameters[r].length; c++){
        if (r>=c){
          assertLargeEntry(clusteredParameters, clusterAssignments[3], r, c);
        }
        else{
          assertSmallEntry(clusteredParameters, clusterAssignments[3], r, c);
        }
      }
    }
    // one cluster has strong diagonal and upper right    
    for (int r=0; r<clusteredParameters.length; r++){
        for (int c=0; c<clusteredParameters[r].length; c++){
          if (r<=c){
            assertLargeEntry(clusteredParameters, clusterAssignments[6], r, c);
          }
          else{
            assertSmallEntry(clusteredParameters, clusterAssignments[6], r, c);
          }
        }
      }
  }
  

  private static void assertLargeEntry(double[][][] mat, int dim1, int dim2, int dim3){
    Assertions.assertThat(mat[dim1][dim2][dim3]).isGreaterThan(0.3);
  }
  private static void assertSmallEntry(double[][][] mat, int dim1, int dim2, int dim3){
    Assertions.assertThat(mat[dim1][dim2][dim3]).isLessThan(0.3);
  }
}
