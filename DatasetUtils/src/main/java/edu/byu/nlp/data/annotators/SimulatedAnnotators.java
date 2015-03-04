package edu.byu.nlp.data.annotators;

import java.lang.reflect.Type;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import edu.byu.nlp.util.DoubleArrays;
import edu.byu.nlp.util.Matrices;

public class SimulatedAnnotators {

	public static List<SimulatedAnnotator> from(int[][][] confusionMatrices) {
		Preconditions.checkNotNull(confusionMatrices);
		
		long numTotalAnnotations = Matrices.sum(confusionMatrices);
		List<SimulatedAnnotator> annotators = Lists.newArrayList();
		for (int j=0; j<confusionMatrices.length; j++){
			// how many annotations did j contribute?
			double numJAnnotations = Matrices.sum(confusionMatrices[j]);
			double annotationRate = numJAnnotations/numTotalAnnotations;
			
			// normalized confusion matrix
			double[][] confusions = Matrices.convertInt2Double(confusionMatrices[j]);
			Matrices.divideToSelf(confusions, numJAnnotations);
			
			// add to an annotator
			annotators.add(new SimulatedAnnotator(confusions, annotationRate));
		}
		return annotators;
	}
	
	public static List<SimulatedAnnotator> from(double[][][] annotatorConfusions, double[] annotationRates) {
		Preconditions.checkNotNull(annotatorConfusions);
		Preconditions.checkNotNull(annotationRates);
		Preconditions.checkArgument(annotatorConfusions.length==annotationRates.length);
		
		List<SimulatedAnnotator> annotators = Lists.newArrayList();
		for (int j=0; j<annotatorConfusions.length; j++){
			// add to an annotator
			annotators.add(new SimulatedAnnotator(annotatorConfusions[j], annotationRates[j]));
		}
		return annotators;
	}

	public static String serialize(List<SimulatedAnnotator> annotators){
		return new Gson().toJson(annotators);
	}

	public static List<SimulatedAnnotator> deserialize(String serializedAnnotators) {
	    @SuppressWarnings("serial")
		Type type = new TypeToken<List<SimulatedAnnotator>>(){}.getType();
		return new Gson().fromJson(serializedAnnotators, type);
	}

	public static double[][][] confusionsOf(List<SimulatedAnnotator> annotators) {
		List<double[][]> confusions = Lists.newArrayList();
		for (SimulatedAnnotator annotator: annotators){
			confusions.add(annotator.getConfusionMatrix());
		}
		return confusions.toArray(new double[][][]{});
	}

	public static double[] annotationRatesOf(List<SimulatedAnnotator> annotators) {
		List<Double> rates = Lists.newArrayList();
		for (SimulatedAnnotator annotator: annotators){
			rates.add(annotator.getAnnotationRate());
		}
		return DoubleArrays.fromList(rates);
	}
	
}
