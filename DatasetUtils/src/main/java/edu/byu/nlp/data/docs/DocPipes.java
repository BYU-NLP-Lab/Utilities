/**
 * Copyright 2013 Brigham Young University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.byu.nlp.data.docs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

import org.apache.commons.vfs2.FileObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import edu.byu.nlp.data.FlatInstance;
import edu.byu.nlp.data.pipes.Downcase;
import edu.byu.nlp.data.pipes.FieldIndexer;
import edu.byu.nlp.data.pipes.FilenameToContents;
import edu.byu.nlp.data.pipes.IndexFileToLabeledFileList;
import edu.byu.nlp.data.pipes.IndexerCalculator;
import edu.byu.nlp.data.pipes.JSONFileToAnnotatedDocumentList;
import edu.byu.nlp.data.pipes.LabeledInstancePipe;
import edu.byu.nlp.data.pipes.Pipes;
import edu.byu.nlp.data.pipes.RegexpTokenizer;
import edu.byu.nlp.data.pipes.SerialLabeledInstancePipeBuilder;
import edu.byu.nlp.data.types.SparseFeatureVector;
import edu.byu.nlp.dataset.BasicSparseFeatureVector;
import edu.byu.nlp.io.Files2;
import edu.byu.nlp.util.DoubleArrays;
import edu.byu.nlp.util.Indexer;
import edu.byu.nlp.util.IntArrays;
import edu.byu.nlp.util.Nullable;

/**
 * Creates a dataset from a data source of documents. This includes creating
 * count vectors, performing feature selection, and indexing the labels.
 *
 * @author rah67
 * @author plf1
 * 
 */
public class DocPipes {
	private static final Logger logger = LoggerFactory.getLogger(DocPipes.class);

	private DocPipes() {
	}

	public static LabeledInstancePipe<String, String, String, String> indexToDocPipe(FileObject baseDir) {
		return new SerialLabeledInstancePipeBuilder<String, String, String, String>()
				.add(Pipes.oneToManyLabeledInstancePipe(new IndexFileToLabeledFileList()))
				.add(Pipes.labeledInstanceTransformingPipe(new FilenameToContents(baseDir))).build();
	}

	public static LabeledInstancePipe<String, String, String, String> jsonToDocPipe(String jsonReferencedDataDir) throws FileNotFoundException {
	  return Pipes.oneToManyLabeledInstancePipe(new JSONFileToAnnotatedDocumentList(jsonReferencedDataDir));
	}

	/**
	 * Do feature selection (on the wordIndex itself)
	 */
	public static Indexer<String> selectFeatures(List<FlatInstance<List<List<String>>, String>> data,
			FeatureSelectorFactory<String> featureSelectorFactory, Indexer<String> wordIndex) {
		
		if (featureSelectorFactory != null) {
			// Index before feature selection (we'll need to do it again later
			// after deciding which features to keep)
			// Create count vectors
			Iterable<FlatInstance<SparseFeatureVector, String>> countVectors = Pipes
					.<List<List<String>>, SparseFeatureVector, String> labeledInstanceDataTransformingPipe(
							new CountVectorizer<String>(wordIndex)).apply(data);

			// Feature selection
			int numFeatures = wordIndex.size();
			BitSet features = featureSelectorFactory.newFeatureSelector(numFeatures).processLabeledInstances(
					countVectors);
			logger.info("Number of features before selection = " + numFeatures);
			wordIndex = wordIndex.retain(features);
			logger.info("Number of features after selection = " + wordIndex.size());
		}
		return wordIndex;
	}

	public static Function<List<String>, List<String>> sentenceTransform(final Function<String, String> sentenceTransform) {
    if (sentenceTransform==null){
      return null;
    }
		return new Function<List<String>, List<String>>() {
			@Override
			public List<String> apply(List<String> doc) {
				List<String> xdoc = Lists.newArrayList();
				for (String sent : doc) {
				  String xsent = sentenceTransform.apply(sent);
				  if (xsent!=null){
				    xdoc.add(xsent);
				  }
				}
				return xdoc;
			}
		};
	}
	
	public static Function<List<List<String>>, List<List<String>>> tokenTransform(final Function<String, String> tokenTransform) {
	  if (tokenTransform==null){
	    return null;
	  }
		return new Function<List<List<String>>, List<List<String>>>() {
			@Override
			public List<List<String>> apply(List<List<String>> doc) {
				List<List<String>> xdoc = Lists.newArrayList();
				for (List<String> sent : doc) {
					List<String> xsent = Lists.newArrayList();
					for (String word : sent) {
						String xword = tokenTransform.apply(word);
						if (xword!=null){
						  xsent.add(xword);
						}
					}
					xdoc.add(xsent);
				}
				return xdoc;
			}
		};
	}

	public static Function<List<String>,List<List<String>>> tokenSplitter(final Function<String,List<String>> sentenceSplitter){
	  if (sentenceSplitter==null){
	    return null;
	  }
		return new Function<List<String>, List<List<String>>>() {
			@Override
			public List<List<String>> apply(List<String> doc) {
				List<List<String>> xdoc = Lists.newArrayList();
				for (String sent: doc){
					xdoc.add(sentenceSplitter.apply(sent));
				}
				return xdoc;
			}
		};
	}
	
	public static Function<String, List<String>> McCallumAndNigamTokenizer() {
		return Functions.compose(new RegexpTokenizer("[a-zA-Z]+"), new Downcase());
	}

	
	public static final String ENGLISH_SENTENCE_DETECTOR = "en-sent.bin";
	public static Function<String, List<String>> opennlpSentenceSplitter() throws IOException {
	  File modelFile = Files2.temporaryFileFromResource(DocPipes.class, ENGLISH_SENTENCE_DETECTOR);
		final SentenceDetectorME detector = new SentenceDetectorME(new SentenceModel(modelFile));
		
		return new Function<String, List<String>>() {
			@Override
			public List<String> apply(String doc) {
				ArrayList<String> retval = Lists.newArrayList(detector.sentDetect(doc));
				return retval;
			}
		};
		
	}

  public static Function<double[], SparseFeatureVector> arrayToSparseFeatureVector() {
    return new Function<double[], SparseFeatureVector>() {
      @Override
      public SparseFeatureVector apply(double[] values) {
        int[] indices = IntArrays.sequence(0, values.length);
        return new BasicSparseFeatureVector(indices, values);
      }
    };
  }
  
  public static Function<String, double[]> documentVectorToArray() {
    return new Function<String, double[]>() {
      @Override
      public double[] apply(String input) {
        Preconditions.checkNotNull(input);
        // try for a json-style array
        if (input.contains("[")){
          return DoubleArrays.parseDoubleArray(input);
        }
        // otherwise assume numbers are one per line
        else{
          String[] parts = input.split("\n");
          double[] retval = new double[parts.length];
          for (int i=0; i<parts.length; i++){
            retval[i] = Double.parseDouble(parts[i]);
          }
          return retval;
        }
      }
    };
  }

  /**
   * Pipeline converts from a dataset index directory to labeled documents 
   * composed of tokenized sentences 
   */
  public static LabeledInstancePipe<String, String, List<List<String>>, String> inputSentencePipe(
      LabeledInstancePipe<String, String, String, String> indexToDocPipe, 
      @Nullable Function<String, String> docTransform, 
      @Nullable Function<String, List<String>> sentenceSplitter, 
      @Nullable Function<String, List<String>> tokenizer,
      @Nullable Function<String, String> tokenTransform) {
    return
        // start with a pipeline <indata, inlabel, outdata, outlabel> 
        new SerialLabeledInstancePipeBuilder<String, String, String, String>()
        // convert a file system dataset (string) to document contents (String) and labels (String)
        .add(indexToDocPipe)
        // transform documents (e.g., remove email headers, transform emoticons)
        .addDataTransform(docTransform)
        // split sentences
        .addDataTransform(sentenceSplitter)
        // tokenize documents
        .addDataTransform(DocPipes.tokenSplitter(tokenizer))
        // transform tokens (e.g., remove stopwords, stemmer, remove short words)
        .addDataTransform(DocPipes.tokenTransform(tokenTransform))
        .build();
  }
  
  /**
   * Pipeline converts string data to feature vectors and does feature selection
   */
  public static LabeledInstancePipe<List<List<String>>, String, SparseFeatureVector, Integer> sentence2FeatureVectorPipe(
      List<FlatInstance<List<List<String>>, String>> data, IndexerCalculator<String, String> indexers, 
      Integer featureNormalizationConstant){
    
    Indexer<String> wordIndex = indexers.getWordIndexer();
    Indexer<String> labelIndex = indexers.getLabelIndexer();
    Indexer<Long> instanceIdIndexer = indexers.getInstanceIdIndexer();
    Indexer<Long> annotatorIdIndexer = indexers.getAnnotatorIdIndexer();
    
    return     
        new SerialLabeledInstancePipeBuilder<List<List<String>>, String, List<List<String>>, String>()
        .addLabelTransform(new FieldIndexer<String>(labelIndex))
        .addAnnotatorIdTransform(FieldIndexer.cast2Long(new FieldIndexer<Long>(annotatorIdIndexer)))
        .addInstanceIdTransform(FieldIndexer.cast2Long(new FieldIndexer<Long>(instanceIdIndexer)))
        .addDataTransform(new CountVectorizer<String>(wordIndex))
        .addDataTransform(new CountNormalizer(featureNormalizationConstant))
        .build();
  }
  

	
}
