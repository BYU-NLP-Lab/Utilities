package edu.byu.nlp.data.util;

import java.io.FileNotFoundException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.random.RandomGenerator;
import org.mockito.Mockito;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import edu.byu.nlp.data.docs.CountCutoffFeatureSelectorFactory;
import edu.byu.nlp.data.docs.FeatureSelectorFactory;
import edu.byu.nlp.data.docs.JSONDocumentDatasetBuilder;
import edu.byu.nlp.data.docs.TokenizerPipes;
import edu.byu.nlp.data.pipes.LabeledInstancePipe;
import edu.byu.nlp.data.types.Dataset;

public class JsonDatasetMocker {

	 public static String jsonInstances(long seed){ 
	    List<String> jsonInstances = Lists.newArrayList(
	        // "1" has 2 annotations + label
	      "{\"batch\": 0, \"data\":\"ABC\", \"endTime\":-1, \"label\":\"0\", \"labelObserved\":true,   \"source\":1,     \"startTime\":0 }", // labeled 
	      "{\"batch\": 1, \"data\":\"KLM\", \"endTime\":1, \"annotation\":\"0\", \"annotator\":\"A\", \"source\":1,     \"startTime\":0 }", // annotation to the same doc
	      "{\"batch\": 2, \"data\":\"KLM\", \"endTime\":2, \"annotation\":\"1\", \"annotator\":\"B\", \"source\":1,     \"startTime\":0 }", // annotation to the same doc
	      "{\"batch\": 3, \"data\":\"HIJ\", \"endTime\":-1,                                            \"source\":1,     \"startTime\":0 }", // bare ref to the same doc
	      
	      // "2" has label
	      "{\"batch\": 0, \"data\":\"DEF\", \"endTime\":-1, \"label\":\"1\", \"labelObserved\":true,   \"source\":\"2\", \"startTime\":0 }", // labeled

	      // "3" has label
	      "{\"batch\": 0, \"data\":\"DEF\", \"endTime\":-1, \"label\":\"1\", \"labelObserved\":true,   \"source\":3,     \"startTime\":0 }", // labeled

	      // "4" has 2 annotations
	      "{\"batch\": 0, \"data\":\"HIJ\", \"endTime\":3, \"annotation\":\"0\", \"annotator\":\"A\", \"source\":4,     \"startTime\":0 }", // annotation
	      "{\"batch\": 3, \"data\":\"HIJ\", \"endTime\":4, \"annotation\":\"1\", \"annotator\":\"A\", \"source\":4,     \"startTime\":0 }", // annotation to same doc
	      
	      // "five" has 1 annotation
	      "{\"batch\": 4, \"data\":\"HIJ\", \"endTime\":-1,                                            \"source\":\"five\", \"startTime\":0 }", // bare doc
	      "{\"batch\": 5, \"data\":\"HIJ\", \"endTime\":5, \"annotation\":\"1\", \"annotator\":\"C\", \"source\":\"five\", \"startTime\":0 }", // annotation to same doc
	      
	      // "six" has nothing
	      "{\"batch\": 6, \"data\":\"HIJ\", \"endTime\":-1,                                           \"source\":\"six\", \"startTime\":0 }", // bare doc
	      "{\"batch\": 7, \"data\":\"HIJ\", \"endTime\":-1,                                           \"source\":\"six\", \"startTime\":0 }", // bare doc (redundant with previous)
	      
	      // "7" has 1 annotation
	      "{\"batch\": 0, \"data\":\"KLM\", \"endTime\":6, \"annotation\":\"0\", \"annotator\":\"A\", \"source\":7,     \"startTime\":0 }", // annotation
	      
	      // "8" has 1 annotation
	      "{\"batch\": 0, \"data\":\"NOP\", \"endTime\":7, \"annotation\":\"0\", \"annotator\":\"A\", \"source\":8,     \"startTime\":0 }" // annotation
	    );
		Random rand = new Random(seed);
		Collections.shuffle(jsonInstances, rand); // try different orderings
		return "[ \n" + Joiner.on(", \n").join(jsonInstances) + "]";

	}

	public static Dataset buildTestDatasetFromJson(String jsonString) throws FileNotFoundException {
		Reader jsonReader = new StringReader(jsonString);

		// build dataset
		Function<String, String> docTransform = null;
		LabeledInstancePipe<String, String, List<String>, String> tokenizerPipe = TokenizerPipes.McCallumAndNigam();
		FeatureSelectorFactory<String> featureSelectorFactory = new CountCutoffFeatureSelectorFactory<String>(-1);
		Integer featureNormalizationConstant = 1;
		Function<List<String>, List<String>> tokenTransform = null;
		RandomGenerator rnd = Mockito.mock(RandomGenerator.class);
		JSONDocumentDatasetBuilder builder = new JSONDocumentDatasetBuilder("dummy source", null, jsonReader,
				docTransform, tokenizerPipe, tokenTransform, featureSelectorFactory, featureNormalizationConstant, rnd);
		return builder.dataset();
	}
	
}