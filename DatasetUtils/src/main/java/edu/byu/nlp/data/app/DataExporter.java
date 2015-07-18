/**
 * Copyright 2012 Brigham Young University
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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import edu.byu.nlp.data.docs.DocPipes;
import edu.byu.nlp.data.docs.DocumentDatasetBuilder;
import edu.byu.nlp.data.docs.TopNPerDocumentFeatureSelectorFactory;
import edu.byu.nlp.data.streams.EmailHeaderStripper;
import edu.byu.nlp.data.types.Dataset;
import edu.byu.nlp.data.types.DatasetInstance;
import edu.byu.nlp.data.types.SparseFeatureVector.Entry;
import edu.byu.nlp.io.Files2;
import edu.byu.nlp.io.Writers;
import edu.byu.nlp.util.jargparser.ArgumentParser;
import edu.byu.nlp.util.jargparser.annotations.Option;

/**
 * @author rah67
 *
 */
public class DataExporter {

	private static final Logger logger = LoggerFactory.getLogger(DataExporter.class);
	
	// TODO : share options with ClustererEvaluator
	@Option(help="base directory of the documents")
	private static String basedir = "20_newsgroups";
	
	@Option
	private static String dataset = "reduced_set";
	
	@Option
	private static String split = "all";

	@Option
	private static int minFeaturesToKeepPerDocument = 10;

    // TODO : share code with ClustererEvaluator
    private static Dataset readData(RandomGenerator rnd) throws IOException {
      Function<String, String> tokenTransform = null; // TODO
      Integer featureNormalizationConstant = null;
	Dataset data =
          new DocumentDatasetBuilder(basedir, dataset, split, new EmailHeaderStripper(),
        	  DocPipes.opennlpSentenceSplitter(), DocPipes.McCallumAndNigamTokenizer(), tokenTransform,  
              new TopNPerDocumentFeatureSelectorFactory(minFeaturesToKeepPerDocument), featureNormalizationConstant )
      	  .dataset();
  
      // Print for verification
      // new StandardOutSink<Integer, SparseFeatureVector>().process(pipeAndData.getOutput());
      logger.info("Number of instances = " + data.getInfo().getNumDocuments());
      logger.info("Number of tokens = " + data.getInfo().getNumTokens());
      logger.info("Number of features = " + data.getInfo().getNumFeatures());
      logger.info("Number of classes = " + data.getInfo().getNumClasses());
  
      data.shuffle(rnd);
      return data;
    }

	public static class Instance2SVMLitePlus implements Function<DatasetInstance, String> {
		
		@Override
		public String apply(DatasetInstance instance) {
			StringBuilder sb = new StringBuilder();
			sb.append(instance.getInfo().getSource());
			sb.append(' ');
			sb.append(instance.getObservedLabel());
			for (Entry entry : instance.asFeatureVector().sparseEntries()) {
				sb.append(' ');
				sb.append(entry.getIndex());
				sb.append(":");
				sb.append(entry.getValue());
			}
			return sb.toString();
		}
		
	}
	
	public static void main(String[] args) throws IOException {
		args = new ArgumentParser(DataExporter.class).parseArgs(args).getPositionalArgs();
		
		RandomGenerator rnd = new MersenneTwister();
		Dataset dataset = readData(rnd);
		
		Iterable<String> it = Iterables.transform(dataset, new Instance2SVMLitePlus());
		if (args.length < 1) {
			Writers.writeLines(new PrintWriter(new BufferedOutputStream(System.out)), it);
		} else {
			Files2.writeLines(it, args[0]);
		}
	}
	
}
