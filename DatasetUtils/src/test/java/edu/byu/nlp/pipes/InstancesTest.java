package edu.byu.nlp.pipes;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.base.Functions;

import edu.byu.nlp.annotationinterface.Constants;
import edu.byu.nlp.annotationinterface.java.AnnotationInterfaceJavaUtils;
import edu.byu.nlp.data.FlatInstance;
import edu.byu.nlp.data.FlatLabeledInstance;
import edu.byu.nlp.data.pipes.Instances;

public class InstancesTest {

	@Test
	public void testIdentityTransformedInstance(){
		String data = "dummy data";
		String label = "sports";
		String source = "dummy source";
		FlatInstance<String, String> inst = new FlatLabeledInstance<String,String>(
				AnnotationInterfaceJavaUtils.newLabeledInstance(data, label, source));
		
		Assertions.assertThat(inst.getAnnotator()).isEqualTo(Constants.GOLD_AUTOMATIC_ANNOTATOR_ID);
//		Assertions.assertThat(inst.getInstanceId()).isEqualTo(AnnotationInterfaceJavaUtils.NULL_IDENTIFIER);
		Assertions.assertThat(inst.getSource()).isEqualTo(source);
		Assertions.assertThat(inst.getLabel()).isEqualTo(label);
		Assertions.assertThat(inst.getStartTimestamp()).isEqualTo(AnnotationInterfaceJavaUtils.NULL_TIMESTAMP);
		Assertions.assertThat(inst.getEndTimestamp()).isEqualTo(AnnotationInterfaceJavaUtils.NULL_TIMESTAMP);
		
		Function<String, String> dataF = Functions.identity();
		Function<String, String> labelF = Functions.identity();
		Function<String, String> sourceF = Functions.identity();
		Function<Long, Long> instanceIdF = Functions.identity();
		Function<Long, Long> annotatorIdF = Functions.identity();
		FlatInstance<String, String> tinst = Instances.transformedLabeledInstance(inst, dataF, labelF, sourceF, instanceIdF, annotatorIdF);

		Assertions.assertThat(tinst.getAnnotator()).isEqualTo(Constants.GOLD_AUTOMATIC_ANNOTATOR_ID);
//		Assertions.assertThat(tinst.getInstanceId()).isEqualTo(AnnotationInterfaceJavaUtils.NULL_IDENTIFIER);
		Assertions.assertThat(tinst.getSource()).isEqualTo(source);
		Assertions.assertThat(tinst.getLabel()).isEqualTo(label);
		Assertions.assertThat(tinst.getStartTimestamp()).isEqualTo(AnnotationInterfaceJavaUtils.NULL_TIMESTAMP);
		Assertions.assertThat(tinst.getEndTimestamp()).isEqualTo(AnnotationInterfaceJavaUtils.NULL_TIMESTAMP);
		
	}


	@Test
	public void testDataTransformedInstance(){
		String data = "dummy data";
		String label = "sports";
		String source = "dummy source";
		FlatInstance<String, String> inst = new FlatLabeledInstance<String,String>(
				AnnotationInterfaceJavaUtils.newLabeledInstance(data, label, source));
		
		// before transform
		Assertions.assertThat(inst.getAnnotator()).isEqualTo(Constants.GOLD_AUTOMATIC_ANNOTATOR_ID);
//		Assertions.assertThat(inst.getInstanceId()).isEqualTo(AnnotationInterfaceJavaUtils.NULL_IDENTIFIER);
		Assertions.assertThat(inst.getSource()).isEqualTo(source);
		Assertions.assertThat(inst.getLabel()).isEqualTo(label);
		Assertions.assertThat(inst.getStartTimestamp()).isEqualTo(AnnotationInterfaceJavaUtils.NULL_TIMESTAMP);
		Assertions.assertThat(inst.getEndTimestamp()).isEqualTo(AnnotationInterfaceJavaUtils.NULL_TIMESTAMP);
		Assertions.assertThat(inst.getData()).isEqualTo(data);
		
		Function<String, String> dataF = new Function<String, String>() {
			@Override
			public String apply(String input) {
				return "transformed";
			}
		};
		Function<String, String> labelF = Functions.identity();
		Function<String, String> sourceF = Functions.identity();
		Function<Long, Long> instanceIdF = Functions.identity();
		Function<Long, Long> annotatorIdF = Functions.identity();
		FlatInstance<String, String> tinst = Instances.transformedLabeledInstance(inst, dataF, labelF, sourceF, instanceIdF, annotatorIdF);

		// after transform
		Assertions.assertThat(tinst.getAnnotator()).isEqualTo(Constants.GOLD_AUTOMATIC_ANNOTATOR_ID);
//		Assertions.assertThat(tinst.getInstanceId()).isEqualTo(AnnotationInterfaceJavaUtils.NULL_IDENTIFIER);
		Assertions.assertThat(tinst.getSource()).isEqualTo(source);
		Assertions.assertThat(tinst.getLabel()).isEqualTo(label);
		Assertions.assertThat(tinst.getStartTimestamp()).isEqualTo(AnnotationInterfaceJavaUtils.NULL_TIMESTAMP);
		Assertions.assertThat(tinst.getEndTimestamp()).isEqualTo(AnnotationInterfaceJavaUtils.NULL_TIMESTAMP);
		Assertions.assertThat(tinst.getData()).isEqualTo("transformed");
	}
	

	@Test
	public void testLabelTransformedInstance(){
		String data = "dummy data";
		String label = "sports";
		String source = "dummy source";
		FlatInstance<String, String> inst = new FlatLabeledInstance<String,String>(
				AnnotationInterfaceJavaUtils.newLabeledInstance(data, label, source));
		
		// before transform
		Assertions.assertThat(inst.getAnnotator()).isEqualTo(Constants.GOLD_AUTOMATIC_ANNOTATOR_ID);
//		Assertions.assertThat(inst.getInstanceId()).isEqualTo(AnnotationInterfaceJavaUtils.NULL_IDENTIFIER);
		Assertions.assertThat(inst.getSource()).isEqualTo(source);
		Assertions.assertThat(inst.getLabel()).isEqualTo(label);
		Assertions.assertThat(inst.getStartTimestamp()).isEqualTo(AnnotationInterfaceJavaUtils.NULL_TIMESTAMP);
		Assertions.assertThat(inst.getEndTimestamp()).isEqualTo(AnnotationInterfaceJavaUtils.NULL_TIMESTAMP);
		Assertions.assertThat(inst.getData()).isEqualTo(data);
		
		Function<String, String> dataF = Functions.identity();
		Function<String, String> labelF = new Function<String, String>() {
			@Override
			public String apply(String input) {
				return "transformed";
			}
		};
		Function<String, String> sourceF = Functions.identity();
		Function<Long, Long> instanceIdF = Functions.identity();
		Function<Long, Long> annotatorIdF = Functions.identity();
		FlatInstance<String, String> tinst = Instances.transformedLabeledInstance(inst, dataF, labelF, sourceF, instanceIdF, annotatorIdF);

		// after transform
		Assertions.assertThat(tinst.getAnnotator()).isEqualTo(Constants.GOLD_AUTOMATIC_ANNOTATOR_ID);
//		Assertions.assertThat(tinst.getInstanceId()).isEqualTo(AnnotationInterfaceJavaUtils.NULL_IDENTIFIER);
		Assertions.assertThat(tinst.getSource()).isEqualTo(source);
		Assertions.assertThat(tinst.getLabel()).isEqualTo("transformed");
		Assertions.assertThat(tinst.getStartTimestamp()).isEqualTo(AnnotationInterfaceJavaUtils.NULL_TIMESTAMP);
		Assertions.assertThat(tinst.getEndTimestamp()).isEqualTo(AnnotationInterfaceJavaUtils.NULL_TIMESTAMP);
		Assertions.assertThat(tinst.getData()).isEqualTo(data);
	}

	@Test
	public void testSourceTransformedInstance(){
		String data = "dummy data";
		String label = "sports";
		String source = "dummy source";
		FlatInstance<String, String> inst = new FlatLabeledInstance<String,String>(
				AnnotationInterfaceJavaUtils.newLabeledInstance(data, label, source));
		
		// before transform
		Assertions.assertThat(inst.getAnnotator()).isEqualTo(Constants.GOLD_AUTOMATIC_ANNOTATOR_ID);
//		Assertions.assertThat(inst.getInstanceId()).isEqualTo(AnnotationInterfaceJavaUtils.NULL_IDENTIFIER);
		Assertions.assertThat(inst.getSource()).isEqualTo(source);
		Assertions.assertThat(inst.getLabel()).isEqualTo(label);
		Assertions.assertThat(inst.getStartTimestamp()).isEqualTo(AnnotationInterfaceJavaUtils.NULL_TIMESTAMP);
		Assertions.assertThat(inst.getEndTimestamp()).isEqualTo(AnnotationInterfaceJavaUtils.NULL_TIMESTAMP);
		Assertions.assertThat(inst.getData()).isEqualTo(data);
		
		Function<String, String> dataF = Functions.identity();
		Function<String, String> labelF = Functions.identity();
		Function<String, String> sourceF = new Function<String, String>() {
			@Override
			public String apply(String input) {
				return "transformed";
			}
		};
		Function<Long, Long> instanceIdF = Functions.identity();
		Function<Long, Long> annotatorIdF = Functions.identity();
		FlatInstance<String, String> tinst = Instances.transformedLabeledInstance(inst, dataF, labelF, sourceF, instanceIdF, annotatorIdF);

		// after transform
		Assertions.assertThat(tinst.getAnnotator()).isEqualTo(Constants.GOLD_AUTOMATIC_ANNOTATOR_ID);
//		Assertions.assertThat(tinst.getInstanceId()).isEqualTo(AnnotationInterfaceJavaUtils.NULL_IDENTIFIER);
		Assertions.assertThat(tinst.getSource()).isEqualTo("transformed");
		Assertions.assertThat(tinst.getLabel()).isEqualTo(label);
		Assertions.assertThat(tinst.getStartTimestamp()).isEqualTo(AnnotationInterfaceJavaUtils.NULL_TIMESTAMP);
		Assertions.assertThat(tinst.getEndTimestamp()).isEqualTo(AnnotationInterfaceJavaUtils.NULL_TIMESTAMP);
		Assertions.assertThat(tinst.getData()).isEqualTo(data);
	}

	@Test
	public void testInstanceIdTransformedInstance(){
		String data = "dummy data";
		String label = "sports";
		String source = "dummy source";
		FlatInstance<String, String> inst = new FlatLabeledInstance<String,String>(
				AnnotationInterfaceJavaUtils.newLabeledInstance(data, label, source));
		
		// before transform
		Assertions.assertThat(inst.getAnnotator()).isEqualTo(Constants.GOLD_AUTOMATIC_ANNOTATOR_ID);
//		Assertions.assertThat(inst.getInstanceId()).isEqualTo(AnnotationInterfaceJavaUtils.NULL_IDENTIFIER);
		Assertions.assertThat(inst.getSource()).isEqualTo(source);
		Assertions.assertThat(inst.getLabel()).isEqualTo(label);
		Assertions.assertThat(inst.getStartTimestamp()).isEqualTo(AnnotationInterfaceJavaUtils.NULL_TIMESTAMP);
		Assertions.assertThat(inst.getEndTimestamp()).isEqualTo(AnnotationInterfaceJavaUtils.NULL_TIMESTAMP);
		Assertions.assertThat(inst.getData()).isEqualTo(data);
		
		Function<String, String> dataF = Functions.identity();
		Function<String, String> labelF = Functions.identity();
		Function<String, String> sourceF = Functions.identity();
		Function<Long, Long> instanceIdF = new Function<Long, Long>() {
			@Override
			public Long apply(Long input) {
				return (long)42;
			}
		};;
		Function<Long, Long> annotatorIdF = Functions.identity();
		FlatInstance<String, String> tinst = Instances.transformedLabeledInstance(inst, dataF, labelF, sourceF, instanceIdF, annotatorIdF);

		// after transform
		Assertions.assertThat(tinst.getAnnotator()).isEqualTo(Constants.GOLD_AUTOMATIC_ANNOTATOR_ID);
		Assertions.assertThat(tinst.getInstanceId()).isEqualTo(42);
		Assertions.assertThat(tinst.getSource()).isEqualTo(source);
		Assertions.assertThat(tinst.getLabel()).isEqualTo(label);
		Assertions.assertThat(tinst.getStartTimestamp()).isEqualTo(AnnotationInterfaceJavaUtils.NULL_TIMESTAMP);
		Assertions.assertThat(tinst.getEndTimestamp()).isEqualTo(AnnotationInterfaceJavaUtils.NULL_TIMESTAMP);
		Assertions.assertThat(tinst.getData()).isEqualTo(data);
	}
	

	@Test
	public void testAnnotatorIdTransformedInstance(){
		String data = "dummy data";
		String label = "sports";
		String source = "dummy source";
		FlatInstance<String, String> inst = new FlatLabeledInstance<String,String>(
				AnnotationInterfaceJavaUtils.newLabeledInstance(data, label, source));
		
		// before transform
		Assertions.assertThat(inst.getAnnotator()).isEqualTo(Constants.GOLD_AUTOMATIC_ANNOTATOR_ID);
//		Assertions.assertThat(inst.getInstanceId()).isEqualTo(AnnotationInterfaceJavaUtils.NULL_IDENTIFIER);
		Assertions.assertThat(inst.getSource()).isEqualTo(source);
		Assertions.assertThat(inst.getLabel()).isEqualTo(label);
		Assertions.assertThat(inst.getStartTimestamp()).isEqualTo(AnnotationInterfaceJavaUtils.NULL_TIMESTAMP);
		Assertions.assertThat(inst.getEndTimestamp()).isEqualTo(AnnotationInterfaceJavaUtils.NULL_TIMESTAMP);
		Assertions.assertThat(inst.getData()).isEqualTo(data);
		
		Function<String, String> dataF = Functions.identity();
		Function<String, String> labelF = Functions.identity();
		Function<String, String> sourceF = Functions.identity();
		Function<Long, Long> instanceIdF = Functions.identity();
		Function<Long, Long> annotatorIdF = new Function<Long, Long>() {
			@Override
			public Long apply(Long input) {
				return (long)42;
			}
		};
		FlatInstance<String, String> tinst = Instances.transformedLabeledInstance(inst, dataF, labelF, sourceF, instanceIdF, annotatorIdF);

		// after transform
		Assertions.assertThat(tinst.getAnnotator()).isEqualTo(42);
//		Assertions.assertThat(tinst.getInstanceId()).isEqualTo(AnnotationInterfaceJavaUtils.NULL_IDENTIFIER);
		Assertions.assertThat(tinst.getSource()).isEqualTo(source);
		Assertions.assertThat(tinst.getLabel()).isEqualTo(label);
		Assertions.assertThat(tinst.getStartTimestamp()).isEqualTo(AnnotationInterfaceJavaUtils.NULL_TIMESTAMP);
		Assertions.assertThat(tinst.getEndTimestamp()).isEqualTo(AnnotationInterfaceJavaUtils.NULL_TIMESTAMP);
		Assertions.assertThat(tinst.getData()).isEqualTo(data);
	}
	
}
