package edu.byu.nlp.dataset;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.math3.linear.RealMatrixPreservingVisitor;
import org.apache.commons.math3.random.RandomGenerator;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;

import edu.byu.nlp.annotationinterface.Constants;
import edu.byu.nlp.annotationinterface.java.AnnotationInterfaceJavaUtils;
import edu.byu.nlp.data.FlatInstance;
import edu.byu.nlp.data.FlatLabeledInstance;
import edu.byu.nlp.data.types.AnnotationSet;
import edu.byu.nlp.data.types.Dataset;
import edu.byu.nlp.data.types.DatasetInfo;
import edu.byu.nlp.data.types.DatasetInstance;
import edu.byu.nlp.data.types.SparseFeatureVector;
import edu.byu.nlp.data.types.SparseFeatureVector.EntryVisitor;
import edu.byu.nlp.math.SparseRealMatrices;
import edu.byu.nlp.util.DoubleArrays;
import edu.byu.nlp.util.Doubles;
import edu.byu.nlp.util.Enumeration;
import edu.byu.nlp.util.Indexer;
import edu.byu.nlp.util.Indexers;
import edu.byu.nlp.util.IntArrays;
import edu.byu.nlp.util.Integers;
import edu.byu.nlp.util.Iterables2;
import edu.byu.nlp.util.Multisets2;
import edu.byu.nlp.util.Pair;
import edu.byu.nlp.util.TableCounter;

public class Datasets {
	private static final Logger logger = Logger.getLogger(Datasets.class.getName());

	// distance from a double to the nearest integer to cast to int without failing?
	public static final double INT_CAST_THRESHOLD = 1e-10;
	
	private Datasets() {
	}

//	// Combines the labeled and unlabeled Collections into a single view over
//	// the whole data.
//	public static Collection<SparseFeatureVector> allData(Dataset data) {
//		return Collections3.concat(unlabel(data.labeledData()),
//				data.unlabeledData());
//	}
//
//	public static <L, D> Collection<D> unlabel(
//			Collection<Instance<L, D>> labeled) {
//		return Collections2.transform(labeled, Instances.<L, D> dataExtractor());
//	}
//
//	/** Sums the counts of each feature in both the labeled and unlabeled data **/
//	public static double[] countFeatures(Dataset data) {
//		return countFeatures(allData(data), data.getNumFeatures());
//	}
//
//	public static double[] countFeaturesInLabeledData(Dataset data) {
//		return countFeatures(unlabel(data.labeledData()), data.getNumFeatures());
//	}
//
//	public static double[] countFeaturesInUnlabeledData(Dataset data) {
//		return countFeatures(data.unlabeledData(), data.getNumFeatures());
//	}
//
//	public static double[] countFeatures(Iterable<SparseFeatureVector> it,
//			int numFeatures) {
//		double[] featureCounts = new double[numFeatures];
//		for (SparseFeatureVector instance : it) {
//			instance.addTo(featureCounts);
//		}
//		return featureCounts;
//	}
//
//	public static double[][] countLabelsAndFeatures(Dataset data) {
//		return countLabelsAndFeatures(data.labeledData(), data.getNumLabels(),
//				data.getNumFeatures());
//	}
//
//	public static double[][] countLabelsAndFeatures(
//			Iterable<Instance<Integer, SparseFeatureVector>> it, int numLabels,
//			int numFeatures) {
//		double[][] featureCounts = new double[numLabels][numFeatures];
//		for (Instance<Integer, SparseFeatureVector> instance : it) {
//			instance.getData().addTo(featureCounts[instance.getLabel()]);
//		}
//		return featureCounts;
//	}
//
//	public static double[] countLabels(Dataset data) {
//		// FIXME : doesn't work for clustering
//		return countLabels(data.labeledData(), data.getNumLabels());
//	}
//
//	public static double[] countLabels(
//			Iterable<Instance<Integer, SparseFeatureVector>> it, int numLabels) {
//		double[] counts = new double[numLabels];
//		for (Instance<Integer, SparseFeatureVector> i : it) {
//			++counts[i.getLabel()];
//		}
//		return counts;
//	}
//
//
//
//
//
//
//	public static Collection<Instance<Integer, SparseFeatureVector>> concat(
//			Collection<Instance<Integer, SparseFeatureVector>> inst1,
//			Collection<Instance<Integer, SparseFeatureVector>> inst2) {
//		Collection<Instance<Integer, SparseFeatureVector>> instances = Lists
//				.newArrayList();
//		if (inst1 != null) {
//			instances.addAll(inst1);
//		}
//		if (inst2 != null) {
//			instances.addAll(inst2);
//		}
//		return instances;
//	}
//
//	public static Dataset concat(Dataset data1, Dataset data2,
//			Indexer<String> wordIndex, Indexer<String> labelIndex) {
//		Collection<Instance<Integer, SparseFeatureVector>> labeledData = concat(
//				data1.labeledData(), data2.labeledData());
//		Collection<Instance<Integer, SparseFeatureVector>> unlabeledData = concat(
//				data1.unlabeledInstances(), data2.unlabeledInstances());
//
//		return new Dataset(labeledData, unlabeledData, wordIndex, labelIndex);
//	}
//
//	/**
//	 * Splits data.labeledData into 'folds' evenly sized subsets, selects the
//	 * 'fold'th subset as a test set, and concatenates the rest as a train set.
//	 * 
//	 * returns train,test where both datasets have all unlabeled instances from
//	 * the original data.
//	 */
//	public static List<Dataset> nthFold(Dataset data, int fold, int folds) {
//		Iterator<Collection<Instance<Integer, SparseFeatureVector>>> parts = nthFold(
//				data.labeledData(), fold, folds).iterator();
//
//		Dataset train = new Dataset(parts.next(), // train
//				data.unlabeledInstances(), data.getWordIndex(),
//				data.getLabelIndex());
//
//		Dataset test = new Dataset(parts.next(), // test
//				data.unlabeledInstances(), data.getWordIndex(),
//				data.getLabelIndex());
//
//		return Lists.newArrayList(train, test);
//	}
//
//	@SuppressWarnings("unchecked")
//	public static List<Collection<Instance<Integer, SparseFeatureVector>>> nthFold(
//			Collection<Instance<Integer, SparseFeatureVector>> data, int fold,
//			int folds) {
//		Preconditions.checkArgument(fold < folds);
//		int foldsize = (int) Math.floor(data.size() * (1.0 / folds));
//		int foldstart = fold * foldsize;
//
//		// before, rest = data.split(foldstart)
//		Iterator<? extends Collection<Instance<Integer, SparseFeatureVector>>> parts = Iterables2
//				.partition(data, foldstart).iterator();
//		Collection<Instance<Integer, SparseFeatureVector>> beforeFold = parts
//				.next();
//		Collection<Instance<Integer, SparseFeatureVector>> remainder = parts
//				.next();
//
//		// during, after = rest.split(foldsize)
//		parts = Iterables2.partition(remainder, foldsize).iterator();
//		Collection<Instance<Integer, SparseFeatureVector>> duringFold = parts
//				.next();
//		Collection<Instance<Integer, SparseFeatureVector>> afterFold = parts
//				.next();
//
//		// notinfold = before + after
//		Collection<Instance<Integer, SparseFeatureVector>> notinFold = Lists
//				.newArrayList();
//		notinFold.addAll(beforeFold);
//		notinFold.addAll(afterFold);
//
//		// return notinfold, duringfold
//		return Lists.newArrayList(notinFold, duringFold);
//	}
//
//	public static Set<Long> getAnnotators(Dataset data) {
//		Set<Long> annotators = Sets.newHashSet();
//		for (Instance<Integer, SparseFeatureVector> inst : data.allInstances()) {
//			annotators.addAll(inst.getAnnotations().keySet());
//		}
//		return annotators;
//	}
//
//
//
//
//
//	
//	public static List<Dataset> split(int items) {
//		// Guava's Iterables.partition would be nice to use, but has a few
//		// strange limitations that make it inappropritate
//		// (e.g., it doesn't like splits of size 0, and it limits the second
//		// collection returned to be smaller than or
//		// equal to the first)
//		Iterable<? extends Collection<Instance<Integer, SparseFeatureVector>>> labeled = Iterables2
//				.partition(labeledData, (int) (labeledData.size() * labeledPercent));
//		Iterable<? extends Collection<Instance<Integer, SparseFeatureVector>>> unlabeled = Iterables2
//				.partition(unlabeledData, (int) (unlabeledData.size() * unlabeledPercent));
//		List<Dataset> partitions = Lists.newArrayListWithCapacity(2);
//		for (Pair<? extends Collection<Instance<Integer, SparseFeatureVector>>, ? extends Collection<Instance<Integer, SparseFeatureVector>>> pair : Iterables2
//				.pairUp(labeled, unlabeled)) {
//			partitions.add(new Dataset(pair.getFirst(), pair.getSecond(), wordIndex, labelIndex));
//		}
//		return partitions;
//	}
//	
//	// originally part of classify2.Dataset
//	// TODO(rah67): add a more efficient, generic bootstrap sampler to a utility class and use it
//	public edu.byu.nlp.al.classify2.Dataset bootstrapSample(RandomGenerator rnd) {
//	    Iterable<Instance<Integer, SparseFeatureVector>> sampleLabeled = bootstrapSample(labeledInstances, rnd);
//        Iterable<Instance<Integer, SparseFeatureVector>> sampleUnlabeled = bootstrapSample(unlabeledInstances, rnd);
//	    return new Dataset(sampleLabeled, sampleUnlabeled, numClasses, numFeatures, numInstances);
//    }
//	
//	private Iterable<Instance<Integer, SparseFeatureVector>> bootstrapSample(
//            Iterable<Instance<Integer, SparseFeatureVector>> it, RandomGenerator rnd) {
//        List<Instance<Integer, SparseFeatureVector>> list = Lists.newArrayList(it);
//        Collection<Instance<Integer, SparseFeatureVector>> sample = Lists.newArrayListWithCapacity(list.size()); 
//        for (int i = 0; i < list.size(); i++) {
//            sample.add(list.get(rnd.nextInt(list.size())));
//        }
//	    return sample;
//	}

	/**
	 * Split by percents
	 * 
	 * sum(splitPercents) must be 1
	 */
	public static List<Dataset> split(Dataset dataset, double[] splitPercents){
		Preconditions.checkArgument(Doubles.equals(DoubleArrays.sum(splitPercents),1.0,1e-6),
				"splitPercents must sum to 1");
		
		int numDocs = dataset.getInfo().getNumDocuments();
		int[] sizes = new int[splitPercents.length];
		for (int i=0; i<splitPercents.length; i++){
			// intentional trucating cast (math.floor)
			sizes[i] = (int) (splitPercents[i] * numDocs);
		}
		
		// make up the difference between the numdocs and sum(sizes)
		int i=0;
		while (IntArrays.sum(sizes) < dataset.getInfo().getNumDocuments()){
			sizes[i]++;
			i = (i+1)%splitPercents.length;
		}
		
		return split(dataset, sizes);
	}
	
	/**
	 * Split by sizes
	 * 
	 * sum(splitSizes) must equal numdocs
	 */
	public static List<Dataset> split(Dataset dataset, int[] splitSizes){
		Preconditions.checkArgument(IntArrays.sum(splitSizes)==dataset.getInfo().getNumDocuments(), 
				"split sizes must sum to dataset.numdocs");

		List<Dataset> splits = Lists.newArrayList();
		Iterator<DatasetInstance> itr = dataset.iterator();
		
		for (int size: splitSizes){
			List<DatasetInstance> instances = Lists.newArrayList();
			for (int i=0; i<size; i++){
				instances.add(itr.next());
			}
			splits.add(new BasicDataset(instances, infoWithUpdatedCounts(instances, dataset.getInfo())));
		}
		
		return splits;
	}
	
	public static Dataset shuffled(Dataset dataset, RandomGenerator rnd){
		Dataset copy = new BasicDataset(dataset);
		copy.shuffle(rnd);
		return copy;
	}

	
	/**
	 * creates a dataset from a list of FlatInstance. Assumes that information 
	 * in the instances has already been indexed by 
	 * featureIndex, labelIndex, instanceIdIndex, and annotatorIdIndex. 
	 * These are only kept around in order to be available to index new 
	 * data in terms of the dataset.
	 * 
	 * TODO: add support for regressand annotations
	 */
	public static Dataset convert(
			String datasetSource,
			Iterable<FlatInstance<SparseFeatureVector, Integer>> flatInstances,
			Indexer<String> featureIndex, Indexer<String> labelIndex, 
			Indexer<Long> instanceIdIndex, Indexer<Long> annotatorIdIndex, 
			boolean preserveRawAnnotations) {
		
		TableCounter<Long, Long, Integer> annotationCounter = TableCounter.create();
		Multimap<Long, FlatInstance<SparseFeatureVector,Integer>> rawAnnotationMap = HashMultimap.create(); 
		Set<Long> instanceIndices = Sets.newHashSet();
		Set<Long> indicesWithObservedLabel = Sets.newHashSet();
		Map<Long,Integer> labelMap = Maps.newHashMap();
		Map<Long,String> sourceMap = Maps.newHashMap();
		Map<Long,SparseFeatureVector> featureMap = Maps.newHashMap();
		
		// calculate maps in order to aggregate FlatInstances into a Dataset
		// in the FlatInstance representation, a whole list of annotations could be 
		// referring to the same instance. In a Dataset object, all of these are 
		// aggregated into a single instance.
		for (FlatInstance<SparseFeatureVector, Integer> inst: flatInstances){
			// anotations
			if (inst.isAnnotation()){
				long instanceIndex = inst.getInstanceId();
				long annotatorIndex = inst.getAnnotator();
				
				instanceIndices.add(instanceIndex);
				// record instance features (in case this is a previously-unseen instance)
				if (inst.getData()!=null){
					featureMap.put(instanceIndex, inst.getData());
				}
				// record instance source
				if (inst.getSource()!=null){
					sourceMap.put(instanceIndex, inst.getSource());
				}
				// record annotation
				annotationCounter.incrementCount(instanceIndex, annotatorIndex, inst.getLabel());
				if (preserveRawAnnotations){
					rawAnnotationMap.put(instanceIndex, inst);
				}
			}
			// labels
			else{
				long instanceId = inst.getInstanceId();
				instanceIndices.add(instanceId);
				
				// record label
				long labelerId = inst.getAutomaticLabelerId();
				if (labelerId == Constants.CONCEALED_GOLD_AUTOMATIC_ANNOTATOR || 
						labelerId == Constants.OBSERVED_GOLD_AUTOMATIC_ANNOTATOR){
					// this is a trusted gold annotation
					labelMap.put(instanceId, inst.getLabel());
				}
				else{
					logger.warning("untrusted label by labeler "+labelerId+" is being ignored");
				}
				
				if (labelerId == Constants.OBSERVED_GOLD_AUTOMATIC_ANNOTATOR){
					// this is a publically known annotation (available as training data)
					indicesWithObservedLabel.add(instanceId);
				}
				// record instance features
				if (inst.getData()!=null){
					featureMap.put(instanceId, inst.getData());
				}
				// record instance source
				if (inst.getSource()!=null){
					sourceMap.put(instanceId, inst.getSource());
				}
			}
		}
		
		// build dataset
		List<DatasetInstance> instances = Lists.newArrayList();
		for (long instanceIndex: instanceIndices){
			Preconditions.checkState(featureMap.containsKey(instanceIndex),"one instance had no associated data: "+instanceIndex);
			
			// aggregated annotations
			final AnnotationSet annotationSet = BasicAnnotationSet.fromCountTable(
					instanceIndex, annotatorIdIndex.size(), labelIndex.size(), annotationCounter, rawAnnotationMap.get(instanceIndex));
			
			// dataset instance
			DatasetInstance inst = new BasicDatasetInstance(
					featureMap.get(instanceIndex), 
					labelMap.get(instanceIndex), 
					!indicesWithObservedLabel.contains(instanceIndex), // is label concealed
					null, // regressand
					!indicesWithObservedLabel.contains(instanceIndex), // is regressand concealed
					annotationSet, 
					instanceIndex, 
					sourceMap.get(instanceIndex),
					labelIndex);
			
			instances.add(inst);
			
		}
		
		// info without counts
		DatasetInfo info = new BasicDataset.Info(datasetSource, 0,0,0,0,0,0,0,0, annotatorIdIndex, featureIndex, labelIndex, instanceIdIndex);
		
		// dataset with correct counts
		return new BasicDataset(instances, infoWithUpdatedCounts(instances, info));
	}

	public static Pair<? extends Dataset, ? extends Dataset> divideInstancesWithAnnotations(Dataset dataset){
		List<DatasetInstance> annotatedData = Lists.newArrayList();
		List<DatasetInstance> unannotatedData = Lists.newArrayList();
		for (DatasetInstance inst: dataset){
			if (inst.hasAnnotations()){
				annotatedData.add(inst);
			}
			else{
				unannotatedData.add(inst);
			}
		}
		
		return Pair.of(new BasicDataset(annotatedData, infoWithUpdatedCounts(annotatedData, dataset.getInfo())),
				new BasicDataset(unannotatedData, infoWithUpdatedCounts(unannotatedData, dataset.getInfo())));
	}
	
	public static Pair<? extends Dataset, ? extends Dataset> divideInstancesWithLabels(Dataset dataset){
		// can't take a shortcut here because cached values don't take into account concealed labels into account
		
		List<DatasetInstance> labeledData = Lists.newArrayList();
		List<DatasetInstance> unlabeledData = Lists.newArrayList();
		for (DatasetInstance inst: dataset){
			if (inst.hasLabel()){
				labeledData.add(inst);
			}
			else{
				unlabeledData.add(inst);
			}
		}
		
		return Pair.of(new BasicDataset(labeledData, infoWithUpdatedCounts(labeledData, dataset.getInfo())),
				new BasicDataset(unlabeledData, infoWithUpdatedCounts(unlabeledData, dataset.getInfo())));
	}
	
	public static Pair<? extends Dataset, ? extends Dataset> divideInstancesWithObservedLabels(Dataset dataset){
		// take a shortcut if all the data is labeled or unlabeled
		if (dataset.getInfo().getNumDocumentsWithoutObservedLabels()==0){
			return Pair.of(
					dataset, // labeled data
					emptyDataset(dataset.getInfo())); // no unlabeled data
		}
		else if (dataset.getInfo().getNumDocumentsWithObservedLabels()==0){
			return Pair.of(
					emptyDataset(dataset.getInfo()), // no labeled data
					dataset); // unlabeled
		}
		
		List<DatasetInstance> labeledData = Lists.newArrayList();
		List<DatasetInstance> unlabeledData = Lists.newArrayList();
		for (DatasetInstance inst: dataset){
			if (inst.hasObservedLabel()){
				labeledData.add(inst);
			}
			else{
				unlabeledData.add(inst);
			}
		}
		
		return Pair.of(new BasicDataset(labeledData, infoWithUpdatedCounts(labeledData, dataset.getInfo())),
				new BasicDataset(unlabeledData, infoWithUpdatedCounts(unlabeledData, dataset.getInfo())));
	}

	public static DatasetInfo infoWithCalculatedCounts(Iterable<DatasetInstance> instances, String source, 
			Indexer<Long> annotatorIdIndexer, Indexer<String> featureIndexer, Indexer<String> labelIndexer,
			Indexer<Long> instanceIdIndexer){
		BasicDataset.Info info = new BasicDataset.Info(source, 0,0,0,0,0,0,0,0, 
				annotatorIdIndexer, featureIndexer, labelIndexer, instanceIdIndexer);
		return infoWithUpdatedCounts(instances, info);
	}
	
	public static DatasetInfo infoWithUpdatedCounts(Iterable<DatasetInstance> instances, DatasetInfo previousInfo){

		int numDocuments = 0, numDocumentsWithAnnotations = 0, numDocumentsWithLabels = 0, numDocumentsWithObservedLabels = 0;
		int numTokens = 0, numTokensWithAnnotations = 0, numTokensWithLabels = 0, numTokensWithObservedLabels = 0;
		for (DatasetInstance inst: instances){
			int numTokensInCurrentDocument = Integers.fromDouble(inst.asFeatureVector().sum(),INT_CAST_THRESHOLD); 
			
			numDocuments++;
			numTokens += numTokensInCurrentDocument;
			if (inst.hasAnnotations()){
				numDocumentsWithAnnotations++;
				numTokensWithAnnotations += numTokensInCurrentDocument;
			}
			if (inst.hasLabel()){
				numDocumentsWithLabels++;
				numTokensWithLabels += numTokensInCurrentDocument;
			}
			if (inst.hasObservedLabel()){
				numDocumentsWithObservedLabels++;
				numTokensWithObservedLabels += numTokensInCurrentDocument;
			}
		}
		
		return new BasicDataset.Info(
				previousInfo.getSource(), 
				numDocuments, numDocumentsWithAnnotations, numDocumentsWithLabels, numDocumentsWithObservedLabels,
				numTokens, numTokensWithAnnotations, numTokensWithLabels, numTokensWithObservedLabels,
				previousInfo.getAnnotatorIdIndexer(), 
				previousInfo.getFeatureIndexer(), 
				previousInfo.getLabelIndexer(), 
				previousInfo.getInstanceIdIndexer());
	}

	public static Dataset join(Dataset... datasets){
		Preconditions.checkNotNull(datasets);
		Preconditions.checkArgument(datasets.length>0);
		
		Iterable<DatasetInstance> instances = Iterables.concat(datasets);
		
		return new BasicDataset(instances, 
				infoWithUpdatedCounts(instances, datasets[0].getInfo()));
	}
	


	/**
	 * Mutates the dataset by clearing all annotation matrices
	 */
	public static void clearAnnotations(Dataset data) {
		
		for (DatasetInstance inst: data){
			SparseRealMatrices.clear(inst.getAnnotations().getLabelAnnotations());
			inst.getInfo().updateAnnotationInfo();
		}
		
	}
	
//	private static AnnotationSet emptyAnnotationSet(DatasetInfo info) {
//		return new BasicAnnotationSet(info.getNumAnnotators(), info.getNumClasses(), Lists.<FlatInstance<SparseFeatureVector, Integer>>newArrayList());
//	}

	/**
	 * Returns a dataset in which only instances with either a 
	 * label or at least one annotation are retained. 
	 */
	public static Dataset removeDataWithoutAnnotationsOrObservedLabels(Dataset data) {
		List<DatasetInstance> instances = Lists.newArrayList();

		for (DatasetInstance inst: data){
			// only keep instance with more than 0 annotations
			if (inst.hasObservedLabel() || inst.hasAnnotations()){
				instances.add(inst);
			}
		}
		
		return new BasicDataset(instances, infoWithUpdatedCounts(instances, data.getInfo()));
	}

	public static DatasetInstance copy(DatasetInstance inst){
		return new BasicDatasetInstance(inst.asFeatureVector(), 
				inst.getLabel(), DatasetInstances.isLabelConcealed(inst), 
				inst.getRegressand(), DatasetInstances.isRegressandConcealed(inst), 
				inst.getAnnotations(), inst.getInfo().getInstanceId(), inst.getInfo().getSource(), inst.getInfo().getLabelIndexer());
	}
	
	
	
	/**
	 * This method attempt to do the minimum amount of work necessary to 
	 * add a new annotation to an existing dataset. 
	 * 
	 * It is assumed that the annotations were generated by an InstanceManager 
	 * that was drawing instances from the Dataset in question. Therefore, 
	 * all quantities in the annotation are already indexed. The instance id 
	 * in the annotation corresponds to an instanceid in the dataset. The label 
	 * must be an integer generated from the labelIndexer of the dataset. The 
	 * annotatorId must be an int from the annotatorIdIndexer of the 
	 * dataset.
	 * 
	 */
	public static synchronized void addAnnotationToDataset(
			Dataset dataset, FlatInstance<SparseFeatureVector, Integer> ann){
		// check that values are indexed values
		Preconditions.checkArgument(ann.getInstanceId() < dataset.getInfo().getInstanceIdIndexer().size(),
				"cannot add annotation with invalid instance id "+ann.getInstanceId()+"."
						+ " Must be between 0 and "+dataset.getInfo().getInstanceIdIndexer().size());
		Preconditions.checkArgument(ann.getAnnotator() < dataset.getInfo().getAnnotatorIdIndexer().size(),
				"cannot add annotation with invalid annotator id "+ann.getAnnotator()+"."
						+ " Must be between 0 and "+dataset.getInfo().getAnnotatorIdIndexer().size());
		Preconditions.checkArgument(ann.getLabel() < dataset.getInfo().getLabelIndexer().size(),
				"cannot add annotation with invalid label "+ann.getLabel()+"."
						+ " Must be between 0 and "+dataset.getInfo().getLabelIndexer().size());
		
		DatasetInstance inst = dataset.lookupInstance(ann.getSource());
		Preconditions.checkNotNull(inst,"attempted to annotate an instance "+ann.getInstanceId()+" "
				+ "that is unknown to the dataset recorder (not in the dataset).");
		Preconditions.checkState(Objects.equal(inst.getInfo().getSource(),ann.getSource()), 
				"The source of the instance that was looked up ("+inst.getInfo().getSource()+
				") doesn't match the src of the annotation ("+ann.getSource()+").");
		
		// increment previous annotation value for this annotator
		SparseRealMatrices.incrementValueAt(inst.getAnnotations().getLabelAnnotations(), 
				(int)ann.getAnnotator(), ann.getLabel(), 1);

		// update instance info
		inst.getInfo().updateAnnotationInfo();
	}
	
	public static synchronized void addAnnotationsToDataset(
			Dataset dataset, Iterable<FlatInstance<SparseFeatureVector, Integer>> annotations){
		for (FlatInstance<SparseFeatureVector, Integer> ann: annotations){
			addAnnotationToDataset(dataset, ann);
		}
	}
			

	public static List<FlatInstance<SparseFeatureVector, Integer>> instancesIn(Dataset dataset) {
		List<FlatInstance<SparseFeatureVector, Integer>> instances = Lists.newArrayList();
		for (DatasetInstance inst: dataset){
			instances.add(new FlatLabeledInstance<SparseFeatureVector, Integer>(
				AnnotationInterfaceJavaUtils.newLabeledInstance(
						inst.asFeatureVector(), inst.getLabel(), 
						inst.getInfo().getInstanceId(), inst.getInfo().getSource(), 
						DatasetInstances.isLabelConcealed(inst))
				));
		}
		return instances;
	}
	
	public static List<FlatInstance<SparseFeatureVector, Integer>> annotationsIn(Dataset dataset) {
		List<FlatInstance<SparseFeatureVector, Integer>> annotations = Lists.newArrayList();
		for (DatasetInstance inst: dataset){
			annotations.addAll(Lists.newArrayList(inst.getAnnotations().getRawLabelAnnotations()));
		}
		return annotations;
	}
	
	public static Collection<String> wordsIn(final DatasetInstance inst, final Indexer<String> featureIndexer){
		final List<String> words = Lists.newArrayList();
		inst.asFeatureVector().visitSparseEntries(new EntryVisitor() {
			@Override
			public void visitEntry(int index, double value) {
				words.add(featureIndexer.get(index));
			}
		});
		return words;
	}

	/**
	 * Extract an array of labels in the order specified by instanceIndices.
	 * Instances that don't exist in instanceIndices are ignored. Any gaps in
	 * the returned array are filled with -1 values.
	 */
	public static int[] concealedLabels(Dataset data,
			final Map<String, Integer> instanceIndices) {
		// all instances
		List<DatasetInstance> instances = Lists.newArrayList(data);

		// ensure there are no instances NOT in the index map
		for (int i = instances.size() - 1; i >= 0; i--) {
			Preconditions.checkArgument(instanceIndices.containsKey(instances.get(i).getInfo().getSource()),
							"Something is wrong. All instances must be in the instance index map.");
		}

		// extract labels (in order defined by instanceIndices)
		int[] gold = IntArrays.repeat(-1, instances.size());
		for (DatasetInstance inst : instances) {
			gold[instanceIndices.get(inst.getInfo().getSource())] = inst.getLabel();
		}

		return gold;
	}
	
	/**
	 * Extract an array of labels in the order specified by instanceIndices.
	 * Instances that don't exist in instanceIndices are ignored. Any gaps in
	 * the returned array are filled with -1 values.
	 */
	public static int[] labels(Dataset data,
			final Map<String, Integer> instanceIndices) {
		// all instances
		List<DatasetInstance> instances = Lists.newArrayList(data);

		// ensure there are no instances NOT in the index map
		for (int i = instances.size() - 1; i >= 0; i--) {
			Preconditions.checkArgument(instanceIndices.containsKey(instances.get(i).getInfo().getSource()),
							"Something is wrong. All instances must be in the instance index map.");
		}

		// extract labels (in order defined by instanceIndices)
		int[] gold = IntArrays.repeat(-1, instances.size());
		for (DatasetInstance inst : instances) {
			gold[instanceIndices.get(inst.getInfo().getSource())] = inst.getObservedLabel();
		}

		return gold;
	}
	

	public static void writeLabeled2Mallet(Dataset dataset, String outPath)
			throws IOException {
		final BufferedWriter bw = Files.newBufferedWriter(Paths.get(outPath),
				Charsets.UTF_8);
		
		Dataset labeledData = divideInstancesWithObservedLabels(dataset).getFirst();

		final Indexer<String> wordIndex = dataset.getInfo().getFeatureIndexer();
		final Indexer<String> labelIndex = dataset.getInfo().getLabelIndexer();
		for (DatasetInstance inst : labeledData) {
			// name
			bw.write(inst.getInfo().getSource());
			bw.write(' ');

			// label
			bw.write(labelIndex.get(inst.getObservedLabel()));
			bw.write(' ');

			// features id1:count1 id2:count2 etc.
			SparseFeatureVector dat = inst.asFeatureVector();
			dat.visitSparseEntries(new EntryVisitor() {
				@Override
				public void visitEntry(int index, double value) {
					try {
						bw.write(wordIndex.get(index) + ":" + value);
						bw.write(' ');
					} catch (IOException e) {
						throw new IllegalStateException("unable to write", e);
					}
				}
			});
			bw.write("\n");
		}
		bw.close();
	}

	public static Dataset readMallet2Labeled(String inPath) throws IOException {
		return readMallet2Labeled(inPath, null, null, null, null);
	}

	public static Dataset readMallet2Labeled(String inPath,
			Indexer<String> labelIndex, Indexer<String> wordIndex, 
			Indexer<Long> instanceIdIndexer, Indexer<Long> annotatorIdIndexer)
			throws IOException {
		final BufferedReader br = Files.newBufferedReader(Paths.get(inPath),
				Charsets.UTF_8);

		if (wordIndex == null) {
			wordIndex = new Indexer<String>();
		}
		if (labelIndex == null) {
			labelIndex = new Indexer<String>();
		}
		if (instanceIdIndexer == null) {
			instanceIdIndexer = new Indexer<Long>();
		}
		if (annotatorIdIndexer == null) {
			annotatorIdIndexer = new Indexer<Long>();
		}
		Collection<DatasetInstance> instances = Lists
				.newArrayList();

		int lineNumber=0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] parts = line.split(" ");
			String source = parts[0];
			String label = parts[1];
			labelIndex.add(label); // index label
			// feature string -> SparseFeatureVector
			int[] indices = new int[parts.length - 2];
			double[] values = new double[parts.length - 2];
			for (int i = 2; i < parts.length; i++) {
				String[] keyval = parts[i].split(":");
				String feature = keyval[0];
				String count = keyval[1];
				wordIndex.add(feature); // index feature
				indices[i - 2] = wordIndex.indexOf(feature);
				values[i - 2] = Double.parseDouble(count);
			}
			SparseFeatureVector data = new BasicSparseFeatureVector(indices, values);
			Double regressand = null;
			AnnotationSet annotations = null;
			int instanceId = lineNumber++;
			// record instance
			instances.add(new BasicDatasetInstance(
					data, labelIndex.indexOf(label), false,  
					regressand, false, 
					annotations, instanceId, source, labelIndex));
		}
		br.close();

		String source = inPath;
		return new BasicDataset(instances, infoWithCalculatedCounts(instances, 
		        source, annotatorIdIndexer, wordIndex, labelIndex, instanceIdIndexer));
	}

	/**
	 * convert annotations in a dataset into an int tensor
	 * a[num_instances][num_annotators][num_classes] where entry a[i][j][k] is
	 * the number of times annotations annotator j annotatoted instance i with
	 * class k.

	 * TODO: use sparse annotation representation instead
	 */
	@Deprecated
	public static int[][][] compileDenseAnnotations(Dataset dataset) {
		int[][][] a = new int[dataset.getInfo().getNumDocuments()][0][];
		for (Enumeration<DatasetInstance> e : Iterables2.enumerate(dataset)) {
			a[e.getIndex()] = compileDenseAnnotations(
					e.getElement(), dataset.getInfo().getNumClasses(), e.getElement().getInfo().getNumAnnotators());
		}
		return a;
	}

	@Deprecated
	public static int[][] compileDenseAnnotations(
			DatasetInstance instance, int numLabels, int numAnnotators) {
		final int[][] annotations = new int[numAnnotators][numLabels];
		instance.getAnnotations().getLabelAnnotations().walkInOptimizedOrder(new RealMatrixPreservingVisitor() {
			@Override
			public void visit(int row, int column, double value) {
				annotations[row][column] = Integers.fromDouble(value, INT_CAST_THRESHOLD);
			}
			@Override
			public void start(int rows, int columns, int startRow, int endRow,
					int startColumn, int endColumn) {
			}
			@Override
			public double end() {
				return 0;
			}
		});
		return annotations;
	}
	
	public static Dataset emptyDataset(DatasetInfo info){
		return new BasicDataset( // full labeled data
				Lists.<DatasetInstance>newArrayList(), 
				infoWithUpdatedCounts(Lists.<DatasetInstance>newArrayList(), info));
	}

	public static double[] countDocSizes(Dataset data) {
		List<FlatInstance<SparseFeatureVector, Integer>> instances = Datasets.instancesIn(data);
		double[] sizes = new double[instances.size()];
		for (int i = 0; i < sizes.length; i++) {
			sizes[i] = instances.get(i).getData().sum();
		}
		return sizes;
	}
	

	/**
	 * convert a dataset to the following simplified array form:
	 * result=double[num_instances][num_features] so that result[i][f] returns
	 * the count of feature f in document i.
	 * 
	 * Note: all side information such as annotations, labels, and data source
	 * is lost.
	 * 
	 */
	public static double[][] toFeatureArray(Dataset data) {
		double[][] countOfXandF = new double[data.getInfo().getNumDocuments()][];
		int docIndex = 0;
		for (DatasetInstance instance : data) {
			countOfXandF[docIndex] = toFeatureArray(instance,
					data.getInfo().getNumFeatures());
			++docIndex;
		}
		return countOfXandF;
	}

	public static double[] toFeatureArray(
			DatasetInstance instance, int numFeatures) {
		double[] countOfXandF = new double[numFeatures];
		instance.asFeatureVector().addTo(countOfXandF);
		return countOfXandF;
	}
	
	
	public static List<Map<Integer, Double>> toSparseFeatureArray(Dataset data) {
		List<Map<Integer, Double>> retval = Lists.newArrayList();
		for (DatasetInstance inst : data) {
			retval.add(toSparseFeatureArray(inst));
		}
		return retval;
	}

	public static Map<Integer, Double> toSparseFeatureArray(
			DatasetInstance instance) {
		final Map<Integer, Double> sparseFeatures = Maps.newHashMap();
		instance.asFeatureVector().visitSparseEntries(new EntryVisitor() {
			@Override
			public void visitEntry(int index, double value) {
				sparseFeatures.put(index, value);
			}
		});
		return sparseFeatures;
	}

	

	/**
	 * Creates a map from an instance (identity) to that instance's index in the
	 * dataset. This is mainly useful for mapping multi-annotator model
	 * predictions back to instances that may be presented in a different order
	 * than that assumed by the model. Because the multiannotators are
	 * transductive models, we infer labels only for instances that we
	 * specifically ran inference on. Of course, model parameters MAY be used to
	 * generate predictions for unseen instances based on feature values, but
	 * that is a post-hoc process that is different from directly inferring a
	 * label.
	 */
	public static Map<String, Integer> instanceIndices(Dataset data) {
		Map<String, Integer> map = Maps.newHashMap();
		int i = 0;
		for (DatasetInstance instance : data) {
			Preconditions.checkState(
							!map.containsKey(instance.getInfo().getSource()),
							"Dataset contains the same instance (same instance source) twice. This is not allowed!");
			map.put(instance.getInfo().getSource(), i++);
		}
		return map;
	}

	
	

	/**
	 * Move all but N labels per class (if that many labeled annotations per
	 * class exist) from labeledData into unlabeledInstances.
	 */
	public static Dataset hideAllLabelsButNPerClass(Dataset data,
			int numObservedLabelsPerClass, RandomGenerator rnd) {
		Preconditions.checkArgument(numObservedLabelsPerClass>=0,
				"numObservedLabelsPerClass must be non-negative");
		
		Dataset labeledData = Datasets.divideInstancesWithObservedLabels(data).getFirst();
		Multiset<Integer> classCounts = HashMultiset.create();
		Set<Long> chosenInstanceIds = Sets.newHashSet();
		
		// greedily choose a set of labeled data such that at least
		// K=numObservedLabelsPerClass
		// instances have been annotated per class.
		while (classCounts.elementSet().size() < data.getInfo().getNumClasses()
				|| Multisets2.minCount(classCounts) < numObservedLabelsPerClass) {

			// assemble a list of instances that have a class we need.
			// Also, prefer items with at least one annotation
			List<DatasetInstance> candidates = Lists.newArrayList();
			for (DatasetInstance cand : labeledData) {
				if (!chosenInstanceIds.contains(cand.getInfo().getInstanceId()) && // haven't already chosen this
						classCounts.count(cand.getLabel()) < numObservedLabelsPerClass // still need this label
						&& cand.hasAnnotations()) { // prefer if it has annotations
					candidates.add(cand);
				}
			}
			// if we have to, add items that don't have any annotations
			if (candidates.size() == 0) {
				for (DatasetInstance cand : data) {
					if (!chosenInstanceIds.contains(cand.getInfo().getInstanceId()) && // haven't already chosen this
							classCounts.count(cand.getLabel()) < numObservedLabelsPerClass) { // still need this label
						candidates.add(cand);
					}
				}
			}
			// found nothing that will contribute (impossible goal given data)
			if (candidates.size() == 0) {
				logger.warning("Unable to find "
						+ numObservedLabelsPerClass
						+ " labeled instances for each class. Stopping early\n\t"
						+ classCounts);
				break;
			}

			// choose at random among candidates
			DatasetInstance chosen = candidates.get(rnd.nextInt(candidates.size()));
			classCounts.add(chosen.getLabel());
			chosenInstanceIds.add(chosen.getInfo().getInstanceId());
		}
		
		// enforce label hiding / revealing
		List<DatasetInstance> instances = Lists.newArrayList(); // include labeled instances (for now)
		for (DatasetInstance inst: data){
			if (chosenInstanceIds.contains(inst.getInfo().getInstanceId())){
				instances.add(DatasetInstances.instanceWithObservedTruth(inst));
			}
			else{
				instances.add(DatasetInstances.instanceWithConcealedTruth(inst));
			}
		}
		
		return new BasicDataset(instances, Datasets.infoWithUpdatedCounts(instances, data.getInfo()));

	}

	
	/**
	 * Get a dataset that's been transformed to accomodate a larger (or smaller) set 
	 * of annotators, determined by annotatoridIndexer. It is assumed that although 
	 * there may be more of fewer annotators in annotatorIdIndexer than in the dataset, 
	 * existing annotator identity has not changed. That is, the 0th annotator 
	 * in the dataset must be the 0th in the annotatorIdIndexer, etc. 
	 */
	public static Dataset withNewAnnotators(Dataset dataset, Indexer<Long> annotatorIdIndexer){
		Preconditions.checkArgument(Indexers.agree(dataset.getInfo().getAnnotatorIdIndexer(), annotatorIdIndexer),
				"Annotator id indexers conflict");
		
		DatasetInfo info = dataset.getInfo();
		List<DatasetInstance> instances = Lists.newArrayList();
		
		for (DatasetInstance inst: dataset){
			// copy all instances but with an annotationset with more annotators
			AnnotationSet annotationSet = new BasicAnnotationSet(annotatorIdIndexer.size(), info.getNumClasses(), inst.getAnnotations().getRawLabelAnnotations());
			SparseRealMatrices.copyOnto(inst.getAnnotations().getLabelAnnotations(), annotationSet.getLabelAnnotations());
			// instance with the new annotationset
			instances.add(new BasicDatasetInstance(inst.asFeatureVector(), 
					inst.getLabel(), DatasetInstances.isLabelConcealed(inst), 
					inst.getRegressand(), DatasetInstances.isRegressandConcealed(inst), 
					annotationSet, inst.getInfo().getInstanceId(), inst.getInfo().getSource(), dataset.getInfo().getLabelIndexer()));
		}
		
		// dataset with the new instances and the new annotatorIdIndexer
		return new BasicDataset(instances, 
				new BasicDataset.Info(info.getSource(), 
						info.getNumDocuments(), info.getNumDocumentsWithAnnotations(), info.getNumDocumentsWithLabels(), info.getNumDocumentsWithObservedLabels(), 
						info.getNumTokens(), info.getNumTokensWithAnnotations(), info.getNumTokensWithLabels(), info.getNumTokensWithObservedLabels(), 
						annotatorIdIndexer, info.getFeatureIndexer(), info.getLabelIndexer(), info.getInstanceIdIndexer()));
	}

	/**
	 * Converts the dataset's features vectors into sequence representations, 
	 * returning a ragged array indexed by [document][feature_position].
	 * E.g., If features are words, then array[2][7] is the identity (index) of the
	 * word 7 in document 2. Feature ordering within the sequence is arbitrary since 
	 * it is not preserved in the Dataset's feature vectors. 
	 */
	public static int[][] featureVectors2FeatureSequences(Dataset data){
	      int[][] documents = new int[data.getInfo().getNumDocuments()][];
	      for (Enumeration<DatasetInstance> inst: Iterables2.enumerate(data)){
	    	  documents[inst.getIndex()] = SparseFeatureVectors.asSequentialIndices(inst.getElement().asFeatureVector());
	      }
	      return documents;
	}
	
	/**
	 * Returns the sizes of each document assuming that all features are integer-valued. 
	 * If features are not integer-valued, fails.
	 */
	public static int[] integerValuedInstanceSizes(Dataset data){
	      int[] docSizes = new int[data.getInfo().getNumDocuments()];
	      for (Enumeration<DatasetInstance> inst: Iterables2.enumerate(data)){
	    	  docSizes[inst.getIndex()] = Integers.fromDouble(inst.getElement().asFeatureVector().sum(), INT_CAST_THRESHOLD);
	      }
	      return docSizes;
	}
	
	public static Dataset sortedBySource(Dataset data){
    List<DatasetInstance> instances = Lists.newArrayList(data);
    Collections.sort(instances, new Comparator<DatasetInstance>() {
  		@Override
  		public int compare(DatasetInstance o1, DatasetInstance o2) {
  			return o1.getInfo().getSource().compareTo(o2.getInfo().getSource());
  		}
  	});
    return new BasicDataset(instances, data.getInfo());
	}

	public static Dataset filteredDataset(Dataset data, Predicate<DatasetInstance> predicate) {
		Iterable<DatasetInstance> filteredInstances = Lists.newArrayList(Iterables.filter(data, predicate));
		return new BasicDataset(filteredInstances, infoWithUpdatedCounts(filteredInstances, data.getInfo()));
	}

	
	public static Predicate<DatasetInstance> filterNonEmpty(){
		return new Predicate<DatasetInstance>() {
			@Override
			public boolean apply(DatasetInstance inst) {
				if (inst.asFeatureVector().sum()==0){
					logger.warning("Filtering empty document: " + inst.getInfo().getSource());
					return false;
				}
				else{
					return true;
				}
			}
		};
	}
	

	/**
	 * This predicate removes documents from the collection with duplicate sources. In all cases
	 * keep only the first item.
	 */
	public static Predicate<DatasetInstance> filterDuplicateSources(){
		return new Predicate<DatasetInstance>() {
			Set<String> docSources = Sets.newHashSet();
			@Override
			public boolean apply(DatasetInstance inst) {
				if (docSources.contains(inst.getInfo().getSource())){
					logger.warning("Filtering repeated data item " + inst.getInfo().getSource());
					return false;
				}
				else{
					docSources.add(inst.getInfo().getSource());
					return true;
				}
			}
		};
	}

}
