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
import java.io.IOException;
import java.util.BitSet;
import java.util.Map;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import edu.byu.nlp.data.streams.DataStreams;
import edu.byu.nlp.data.streams.DataStreams.Transform;
import edu.byu.nlp.data.streams.Downcase;
import edu.byu.nlp.data.streams.RegexpTokenizer;
import edu.byu.nlp.data.types.DataStreamInstance;
import edu.byu.nlp.data.types.SparseFeatureVector;
import edu.byu.nlp.dataset.BasicSparseFeatureVector;
import edu.byu.nlp.io.Files2;
import edu.byu.nlp.util.DoubleArrays;
import edu.byu.nlp.util.Indexer;
import edu.byu.nlp.util.IntArrays;

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


	/**
	 * Do feature selection (on the wordIndex itself)
	 */
	public static Indexer<String> selectFeatures(Iterable<Map<String,Object>> data,
			FeatureSelectorFactory featureSelectorFactory, Indexer<String> wordIndex) {
		
		if (featureSelectorFactory != null) {
			// Index before feature selection (we'll need to do it again later
			// after deciding which features to keep)
			// Create count vectors
		  Transform vectorizer = DataStreams.Transforms.transformFieldValue(DataStreamInstance.DATA, new CountVectorizer<String>(wordIndex));
		  Iterable<Map<String, Object>> countVectors = Iterables.transform(data, vectorizer);
		  
			// Feature selection
			int numFeatures = wordIndex.size();
			BitSet features = featureSelectorFactory.newFeatureSelector(numFeatures).process(countVectors);
			logger.info("Number of features before selection = " + numFeatures);
			wordIndex = wordIndex.retain(features);
			logger.info("Number of features after selection = " + wordIndex.size());
		}
		return wordIndex;
	}

//	public static Function<Iterable<String>, Iterable<String>> sentenceTransform(final Function<String, String> sentenceTransform) {
//    if (sentenceTransform==null){
//      return null;
//    }
//		return new Function<Iterable<String>, Iterable<String>>() {
//			@Override
//			public List<String> apply(Iterable<String> doc) {
//				List<String> xdoc = Lists.newArrayList();
//				for (String sent : doc) {
//				  String xsent = sentenceTransform.apply(sent);
//				  if (xsent!=null){
//				    xdoc.add(xsent);
//				  }
//				}
//				return xdoc;
//			}
//		};
//	}
//	
//	public static Function<Iterable<Iterable<String>>, Iterable<Iterable<String>>> tokenTransform(final Function<String, String> tokenTransform) {
//	  if (tokenTransform==null){
//	    return null;
//	  }
//		return new Function<Iterable<Iterable<String>>, Iterable<Iterable<String>>>() {
//			@Override
//			public Iterable<Iterable<String>> apply(Iterable<Iterable<String>> doc) {
//				List<Iterable<String>> xdoc = Lists.newArrayList();
//				for (Iterable<String> sent : doc) {
//					List<String> xsent = Lists.newArrayList();
//					for (String word : sent) {
//						String xword = tokenTransform.apply(word);
//						if (xword!=null){
//						  xsent.add(xword);
//						}
//					}
//					xdoc.add(xsent);
//				}
//				return xdoc;
//			}
//		};
//	}
//
//	public static Function<Iterable<String>,Iterable<Iterable<String>>> tokenSplitter(final Function<String,Iterable<String>> sentenceSplitter){
//	  if (sentenceSplitter==null){
//	    return null;
//	  }
//		return new Function<Iterable<String>, Iterable<Iterable<String>>>() {
//			@Override
//			public List<Iterable<String>> apply(Iterable<String> doc) {
//				List<Iterable<String>> xdoc = Lists.newArrayList();
//				for (String sent: doc){
//					xdoc.add(sentenceSplitter.apply(sent));
//				}
//				return xdoc;
//			}
//		};
//	}
	
	public static Function<String, Iterable<String>> McCallumAndNigamTokenizer() {
		return Functions.compose(new RegexpTokenizer("[a-zA-Z]+"), new Downcase());
	}

	
	public static final String ENGLISH_SENTENCE_DETECTOR = "en-sent.bin";
	public static Function<String, Iterable<String>> opennlpSentenceSplitter() throws IOException {
	  File modelFile = Files2.temporaryFileFromResource(DocPipes.class, ENGLISH_SENTENCE_DETECTOR);
		final SentenceDetectorME detector = new SentenceDetectorME(new SentenceModel(modelFile));
		
		return new Function<String, Iterable<String>>() {
			@Override
			public Iterable<String> apply(String doc) {
				return Lists.newArrayList(detector.sentDetect(doc));
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

//  /**
//   * Pipeline converts from a dataset index directory to labeled documents 
//   * composed of tokenized sentences 
//   */
//  public static Function<Map<String, Object>, Map<String, Object>> inputSentencePipe(
//      @Nullable Function<String, String> docTransform, 
//      @Nullable Function<String, List<String>> sentenceSplitter, 
//      @Nullable Function<String, List<String>> tokenizer,
//      @Nullable Function<String, String> tokenTransform) {
//
//      return 
//          Functions2.compose(
//              // transform documents (e.g., remove email headers, transform emoticons)
//              DataStreams.Transforms.transformFieldValue(DataStreamInstance.DATA, String.class, String.class, docTransform),
//              // split sentences
//              DataStreams.Transforms.transformFieldValue(DataStreamInstance.DATA, String.class, List.class, sentenceSplitter),
//              // tokenize documents
//              DataStreams.Transforms.transformFieldValue(DataStreamInstance.DATA, List.class, List.class, tokenizer),
//              // transform tokens (e.g., remove stopwords, stemmer, remove short words)
//              DataStreams.Transforms.transformFieldValue(DataStreamInstance.DATA, List tokenTransform));
//  }
//  
//  /**
//   * Pipeline converts string data to feature vectors and does feature selection
//   * @return 
//   */
//  public static Function<Map<String, Object>, Map<String, Object>> sentence2FeatureVectorPipe(
//      List<Map<String,Object>> data, IndexerCalculator<String, String> indexers, 
//      Integer featureNormalizationConstant){
//    
//    Indexer<String> wordIndex = indexers.getWordIndexer();
//    Indexer<String> labelIndex = indexers.getLabelIndexer();
//    Indexer<String> instanceIdIndexer = indexers.getInstanceIdIndexer();
//    Indexer<String> annotatorIdIndexer = indexers.getAnnotatorIdIndexer();
//    
//    return 
//        Functions2.compose(
//            DataStreams.Transforms.transformFieldValue(DataStreamInstance.LABEL, new FieldIndexer<String>(labelIndex)),
//            DataStreams.Transforms.transformFieldValue(DataStreamInstance.ANNOTATION, new FieldIndexer<String>(labelIndex)),
//            DataStreams.Transforms.transformFieldValue(DataStreamInstance.SOURCE, new FieldIndexer<String>(instanceIdIndexer)),
//            DataStreams.Transforms.transformFieldValue(DataStreamInstance.ANNOTATOR, new FieldIndexer<String>(annotatorIdIndexer)),
//            DataStreams.Transforms.transformFieldValue(DataStreamInstance.DATA, new CountVectorizer<String>(wordIndex)),
//            DataStreams.Transforms.transformFieldValue(DataStreamInstance.DATA, new CountNormalizer(featureNormalizationConstant))
//            );
//    
//  }
  

	
}
