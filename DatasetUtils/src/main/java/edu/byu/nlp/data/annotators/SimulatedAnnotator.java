package edu.byu.nlp.data.annotators;



public class SimulatedAnnotator {

	private double[][] confusionMatrix;
	private double annotationRate; // fraction of total annotations that came from this annotator
	
	public SimulatedAnnotator(double[][] confusionMatrix, double annotationRate){
		this.setConfusionMatrix(confusionMatrix);
		this.setAnnotationRate(annotationRate);
	}

	/**
	 * @return the confusionMatrix
	 */
	public double[][] getConfusionMatrix() {
		return confusionMatrix;
	}

	/**
	 * @param confusionMatrix the confusionMatrix to set
	 */
	public void setConfusionMatrix(double[][] confusionMatrix) {
		this.confusionMatrix = confusionMatrix;
	}

	/**
	 * @return the annotationRate
	 */
	public double getAnnotationRate() {
		return annotationRate;
	}

	/**
	 * @param annotationRate the annotationRate to set
	 */
	public void setAnnotationRate(double annotationRate) {
		this.annotationRate = annotationRate;
	}
	
	
}
