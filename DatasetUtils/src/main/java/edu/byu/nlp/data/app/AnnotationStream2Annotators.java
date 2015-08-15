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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomAdaptor;
import org.apache.commons.math3.random.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.byu.nlp.data.annotators.SimulatedAnnotator;
import edu.byu.nlp.data.annotators.SimulatedAnnotators;
import edu.byu.nlp.data.docs.CountCutoffFeatureSelectorFactory;
import edu.byu.nlp.data.docs.DocPipes;
import edu.byu.nlp.data.docs.FeatureSelectorFactories;
import edu.byu.nlp.data.docs.JSONDocumentDatasetBuilder;
import edu.byu.nlp.data.docs.TopNPerDocumentFeatureSelectorFactory;
import edu.byu.nlp.data.types.Dataset;
import edu.byu.nlp.dataset.Datasets;
import edu.byu.nlp.io.Files2;
import edu.byu.nlp.io.Paths;
import edu.byu.nlp.util.DoubleArrays;
import edu.byu.nlp.util.Matrices;
import edu.byu.nlp.util.jargparser.ArgumentParser;
import edu.byu.nlp.util.jargparser.annotations.Option;

/**
 * @author plf1
 *
 * Read in a json annotation stream, output a json file containing fitted 
 * annotator confusion matrices along with the number of annotations 
 * produced by each annotator (for use in future simulations)
 */
public class AnnotationStream2Annotators {
  private static Logger logger = LoggerFactory.getLogger(AnnotationStream2Annotators.class);

  @Option(help = "A json annotation stream containing annotations to be fitted.")
  private static String jsonStream = "/aml/data/plf1/cfgroups/cfgroups1000.json"; 

  @Option(help = "The file where fitted confusion matrices should be written to (in json)")
  private static String output = null; 
  
  public enum ClusteringMethod {NONE, KMEANS, ACCURACY, RANDOM} 
  @Option(help = "")
  private static ClusteringMethod aggregate = ClusteringMethod.NONE;

  @Option(help = "The max number of buckets to aggregate annotators into.")
  private static int k = 5;

  @Option(help = "added to annotation counts before normalizing to avoid zero entries")
  private static double smooth = 0.01;

  @Option(help = "how many iterations should clustering algorithms do?")
  private static int maxIterations = 10000;

  @Option(help = "some choices (kmeans initialization; tie-breaking in majority vote) are stochastic. Seed the RNG.")
  private static long seed = System.currentTimeMillis();
  
  private enum ConfusionMatrixTruth {MAJORITY, GOLD}
  @Option(help = "calculate annotator confusion matrices by comparing their answers with the gold standard. If false, ")
  private static ConfusionMatrixTruth confusionMatrixTruth = ConfusionMatrixTruth.GOLD;
  
  
  public static void main(String[] args) throws IOException{
    // parse CLI arguments
    new ArgumentParser(AnnotationStream2Annotators.class).parseArgs(args);
    Preconditions.checkNotNull(jsonStream,"You must provide a valid --json-stream!");
    Preconditions.checkArgument(smooth>=0,"invalid smoothing value="+smooth);
    Preconditions.checkArgument(k>0,"invalid number of clusters="+k);
    
    // compile annotation stream data into a dataset
    RandomGenerator rnd = new MersenneTwister(seed);
    Dataset data = readData(jsonStream);
    
    // create confusion matrices for each annotator wrt some truth
    int[][][] confusionMatrices; // confusionMatrices[annotator][true label][annotation] = count
    logger.info("dataset="+data);
    switch(confusionMatrixTruth){
    case GOLD:
      confusionMatrices = Datasets.confusionMatricesWrtGoldLabels(data); 
      break;
    case MAJORITY:
      confusionMatrices = Datasets.confusionMatricesWrtMajorityVoteLabels(data, rnd); 
      break;
    default:
      throw new IllegalArgumentException("unknown truth standard for constructing confusion matrices: "+confusionMatrixTruth);
    }
    
    // aggregate annotators based on their confusion matrices
    double[][][] annotatorParameters = confusionMatrices2AnnotatorParameters(confusionMatrices);
    int[] clusterAssignments = clusterAnnotatorParameters(annotatorParameters, aggregate, k, maxIterations, smooth, rnd);
    double[][][] clusteredAnnotatorParameters = aggregateAnnotatorParameterClusters(annotatorParameters, clusterAssignments);

    // aggregate annotator rates
    double[] annotationRates = new double[clusteredAnnotatorParameters.length]; 
    for (int j=0; j<confusionMatrices.length; j++){
    	long numAnnotationsPerJ = Matrices.sum(confusionMatrices[j]);
    	// add this annotator's annotation count to the cluster total
    	annotationRates[clusterAssignments[j]] += numAnnotationsPerJ;
    }
    DoubleArrays.normalizeToSelf(annotationRates);
    
    // output to console 
    logger.info("aggregated annotators=\n"+Matrices.toString(clusteredAnnotatorParameters, 10, 10, 20, 3));
    for (int c=0; c<clusteredAnnotatorParameters.length; c++){
        logger.info("aggregated annotator #"+c+" accuracy="+accuracyOf(clusteredAnnotatorParameters[c]));
        logger.info("aggregated annotator #"+c+" rate="+annotationRates[c]);
    }

    // output to file 
    if (output!=null){
      List<SimulatedAnnotator> annotators = SimulatedAnnotators.from(clusteredAnnotatorParameters, annotationRates);
      Files2.write(SimulatedAnnotators.serialize(annotators), output);
    }
  }
  /////////////////////////////
  // END MAIN
  /////////////////////////////

  public static double[][][] confusionMatrices2AnnotatorParameters(int[][][] confusionMatrices) {
    Preconditions.checkNotNull(confusionMatrices);
    Preconditions.checkArgument(confusionMatrices.length>0);
    int numAnnotators = confusionMatrices.length;
    
    logger.info("num annotators="+numAnnotators);
    logger.info("total annotations="+Matrices.sum(confusionMatrices));

    // smoothed annotation counts
    double[][][] annotatorParameters = Matrices.fromInts(confusionMatrices);
    Matrices.addToSelf(annotatorParameters, smooth);

    // empirical confusion matrices
    Matrices.normalizeRowsToSelf(annotatorParameters);
    
    return annotatorParameters;
  }

  public static int[] clusterAnnotatorParameters(double[][][] annotatorParameters, ClusteringMethod clusteringMethod, int k, int maxIterations, double smooth, RandomGenerator rnd) {
    Preconditions.checkNotNull(annotatorParameters);
    Preconditions.checkArgument(annotatorParameters.length>0);
    int numAnnotators = annotatorParameters.length;
    
    // put each annotator in a singleton cluster
    List<Annotator> annotators = Lists.newArrayList();
    for (int i=0; i<numAnnotators; i++){
    	annotators.add(new Annotator(annotatorParameters[i], i, i));
    }
    
    // precompute potenially useful quantities
    double uniformClusterSize = (double)numAnnotators / k;  
    
    // transformed confusion matrices
    switch(clusteringMethod){
    case NONE:
      break;
    case RANDOM:
      // shuffle, then assign in equal blocks 
      Collections.shuffle(annotators, new RandomAdaptor(rnd));
      for (int c=0; c<k; c++){
        int start = (int)Math.floor(c*uniformClusterSize);
        int end = (int)Math.floor(c*uniformClusterSize+uniformClusterSize);
        for (int a=start; a<end; a++){
        	annotators.get(a).clusterAssignment = c;
        }
      }
      break;
    case ACCURACY:
      logger.debug("sorting annotators by accuracy");
      Collections.sort(annotators); // re-order annotators so that more accurate ones appear first
      for (int i=0; i<annotatorParameters.length; i++){
        logger.debug("annotator #"+i+" accuracy="+accuracyOf(annotatorParameters[i]));
      }
      // now divide annotators into equal chunks--like accuracies will cluster together
      for (int c=0; c<k; c++){
	    int start = (int)Math.floor(c*uniformClusterSize);
	    int end = (int)Math.floor(c*uniformClusterSize+uniformClusterSize);
	    for (int a=start; a<end; a++){
	      annotators.get(a).clusterAssignment = c;
	    }
	  }
      break;
    case KMEANS:
      assignKMeansClusters(annotators, k, maxIterations, rnd);
      break;
    default:
      throw new IllegalArgumentException("unknown aggregation method="+clusteringMethod);
    }
    
    // return the mapping vector
    int[] clusterAssignments = new int[numAnnotators];
    for (int a=0; a<numAnnotators; a++){
    	Annotator annotator = annotators.get(a);
    	clusterAssignments[annotator.index] = annotator.clusterAssignment;
    }
    
    return clusterAssignments;
  }

  
  public static double[][][] aggregateAnnotatorParameterClusters(double[][][] annotatorParameters, int[] clusterAssignments){

    // group clustered parameters
    Map<Integer,Set<double[][]>> clusterMap = Maps.newHashMap();
    for (int i=0; i<clusterAssignments.length; i++){
      int clusterAssignment = clusterAssignments[i];
      if (!clusterMap.containsKey(clusterAssignment)){
        clusterMap.put(clusterAssignment, Sets.<double[][]>newIdentityHashSet());
      }
      clusterMap.get(clusterAssignment).add(annotatorParameters[i]);
    }
    
    // aggregate clustered parameters
    List<double[][]> clusteredAnnotatorParameters = Lists.newArrayList();
    for (Set<double[][]> cluster: clusterMap.values()){
      double[][][] clusterTensor = cluster.toArray(new double[][][]{});
      double[][] averagedConfusions = Matrices.sumOverFirst(clusterTensor);
      Matrices.divideToSelf(averagedConfusions, cluster.size());
      clusteredAnnotatorParameters.add(averagedConfusions);
    }
    
    // re-assign confusions
    return clusteredAnnotatorParameters.toArray(new double[][][]{});
  }
  
  
  /**
   * This returns a set of clustered annotator parameters. Averaging them yields the centroid of the cluster.
   * Note that Annotator.clusterAssignment properties are change IN PLACE.  
   */
  private static void assignKMeansClusters(List<Annotator> annotators, int k, int maxIterations, RandomGenerator rnd) {
    Preconditions.checkNotNull(annotators);
    Preconditions.checkArgument(annotators.size()>0);
    KMeansPlusPlusClusterer<Annotator> clusterer =  new KMeansPlusPlusClusterer<>(k, maxIterations, new EuclideanDistance(), rnd);
    List<CentroidCluster<Annotator>> clusterCentroids = clusterer.cluster(annotators);
    
    for (int c=0; c<clusterCentroids.size(); c++){
      for (Annotator annotator: clusterCentroids.get(c).getPoints()){
        // note: we don't return the centroid point here because averaging the points in the cluster 
        // yields precisely the centroid point.
        // stick this annotator in this location in the confusions
    	annotator.clusterAssignment = c;
      }
    }
    
  }
  

  private static class Annotator implements Clusterable, Comparable<Annotator>{
    final protected double[] flatParameter;
    final protected double accuracy;
    final protected int index;
    protected int clusterAssignment;
    public Annotator(double[][] parameter, int index, int clusterAssignment){
      this.index=index;
      this.accuracy = accuracyOf(parameter);
      this.flatParameter=Matrices.flatten(parameter);
      this.clusterAssignment=clusterAssignment;
    }
    @Override
    public double[] getPoint() {
      return flatParameter;
    }
	@Override
	public int compareTo(Annotator o) {
        return Double.compare(o.accuracy, this.accuracy); // high-to-low
	}
  }
  
  /**
   * average diagonal value
   */
  private static double accuracyOf(double[][] annotatorConfusion){
    return Matrices.trace(annotatorConfusion)/annotatorConfusion.length;
  }
  

  private static Dataset readData(String jsonStream) throws IOException {
    // these parameters are not important since we will ignore the data itself and concentrate only on annotations
    // in this script
    int featureCountCutoff = -1;
    int topNFeaturesPerDocument = -1;
    Integer featureNormalizer = null;
    Function<String, String> docTransform = null;
    Function<String, String> tokenTransform = null;
    
    // data reader pipeline per dataset
    // build a dataset, doing all the tokenizing, stopword removal, and feature normalization
    String folder = Paths.directory(jsonStream);
    String file = Paths.baseName(jsonStream);
    Dataset data = new JSONDocumentDatasetBuilder(folder, file, 
          docTransform, DocPipes.opennlpSentenceSplitter(), DocPipes.McCallumAndNigamTokenizer(), tokenTransform, 
          FeatureSelectorFactories.conjoin(
              new CountCutoffFeatureSelectorFactory<String>(featureCountCutoff), 
              (topNFeaturesPerDocument<0)? null: new TopNPerDocumentFeatureSelectorFactory(topNFeaturesPerDocument)),
          featureNormalizer)
          .dataset();
      
    // Postprocessing: remove all documents with duplicate sources or empty features
    data = Datasets.filteredDataset(data, Predicates.and(Datasets.filterDuplicateSources(), Datasets.filterNonEmpty()));
    
    logger.info("Number of labeled instances = " + data.getInfo().getNumDocumentsWithObservedLabels());
    logger.info("Number of unlabeled instances = " + data.getInfo().getNumDocumentsWithoutObservedLabels());
    logger.info("Number of tokens = " + data.getInfo().getNumTokens());
    logger.info("Number of features = " + data.getInfo().getNumFeatures());
    logger.info("Number of classes = " + data.getInfo().getNumClasses());
    logger.info("Average Document Size = " + (data.getInfo().getNumTokens()/data.getInfo().getNumDocuments()));

    return data;
  }
  
}
