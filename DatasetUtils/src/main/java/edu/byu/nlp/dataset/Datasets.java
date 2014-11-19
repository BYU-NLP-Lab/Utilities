package edu.byu.nlp.dataset;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.byu.nlp.data.FlatInstance;
import edu.byu.nlp.data.types.AnnotationSet;
import edu.byu.nlp.data.types.Dataset;
import edu.byu.nlp.data.types.DatasetInfo;
import edu.byu.nlp.data.types.DatasetInstance;
import edu.byu.nlp.data.types.SparseFeatureVector;
import edu.byu.nlp.util.Indexer;
import edu.byu.nlp.util.Pair;
import edu.byu.nlp.util.TableCounter;

public class Datasets {

//	private static final Logger logger = Logger.getLogger(Datasets.class.getName());

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
//	public static double[] countDocSizes(Dataset data) {
//		ArrayList<Instance<Integer, SparseFeatureVector>> instances = Lists
//				.newArrayList(data.allInstances());
//		double[] sizes = new double[instances.size()];
//		for (int i = 0; i < sizes.length; i++) {
//			sizes[i] = instances.get(i).getData().sum();
//		}
//		return sizes;
//	}
//
//	public static void writeLabeled2Mallet(Dataset dataset, String outPath)
//			throws IOException {
//		final BufferedWriter bw = Files.newBufferedWriter(Paths.get(outPath),
//				Charsets.UTF_8);
//
//		final Indexer<String> wordIndex = dataset.getWordIndex();
//		final Indexer<String> labelIndex = dataset.getLabelIndex();
//		for (Instance<Integer, SparseFeatureVector> inst : dataset
//				.labeledData()) {
//			// name
//			bw.write(inst.getSource());
//			bw.write(' ');
//
//			// label
//			bw.write(labelIndex.get(inst.getLabel()));
//			bw.write(' ');
//
//			// features id1:count1 id2:count2 etc.
//			SparseFeatureVector dat = inst.getData();
//			dat.visitSparseEntries(new EntryVisitor() {
//				@Override
//				public void visitEntry(int index, double value) {
//					try {
//						bw.write(wordIndex.get(index) + ":" + value);
//						bw.write(' ');
//					} catch (IOException e) {
//						throw new IllegalStateException("unable to write", e);
//					}
//				}
//			});
//			bw.write("\n");
//		}
//		bw.close();
//	}
//
//	public static Dataset readMallet2Labeled(String inPath) throws IOException {
//		return readMallet2Labeled(inPath, null, null);
//	}
//
//	public static Dataset readMallet2Labeled(String inPath,
//			Indexer<String> labelIndex, Indexer<String> wordIndex)
//			throws IOException {
//		final BufferedReader br = Files.newBufferedReader(Paths.get(inPath),
//				Charsets.UTF_8);
//
//		if (wordIndex == null) {
//			wordIndex = new Indexer<String>();
//		}
//		if (labelIndex == null) {
//			labelIndex = new Indexer<String>();
//		}
//		Collection<Instance<Integer, SparseFeatureVector>> instances = Lists
//				.newArrayList();
//
//		for (String line = br.readLine(); line != null; line = br.readLine()) {
//			String[] parts = line.split(" ");
//			String source = parts[0];
//			String label = parts[1];
//			labelIndex.add(label); // index label
//			// feature string -> SparseFeatureVector
//			int[] indices = new int[parts.length - 2];
//			double[] values = new double[parts.length - 2];
//			for (int i = 2; i < parts.length; i++) {
//				String[] keyval = parts[i].split(":");
//				String feature = keyval[0];
//				String count = keyval[1];
//				wordIndex.add(feature); // index feature
//				indices[i - 2] = wordIndex.indexOf(feature);
//				values[i - 2] = Double.parseDouble(count);
//			}
//			SparseFeatureVector data = new SparseFeatureVector(indices, values);
//			// record instance
//			instances.add(new BasicInstance<Integer, SparseFeatureVector>(
//					labelIndex.indexOf(label), false, TimedEvent.Zeros(),
//					source, data, null));
//		}
//
//		br.close();
//
//		return new Dataset(instances,
//				Collections
//						.<Instance<Integer, SparseFeatureVector>> emptyList(),
//				wordIndex, labelIndex);
//	}
//
//	/**
//	 * Extract an array of labels in the order specified by instanceIndices.
//	 * Instances that don't exist in instanceIndices are ignored. Any gaps in
//	 * the returned array are filled with -1 values.
//	 */
//	public static int[] labels(Dataset data,
//			final Map<String, Integer> instanceIndices) {
//		// all instances
//		ArrayList<Instance<Integer, SparseFeatureVector>> instances = Lists
//				.newArrayList(data.allInstances());
//
//		// ensure there are no instances NOT in the index map
//		for (int i = instances.size() - 1; i >= 0; i--) {
//			Preconditions
//					.checkArgument(instanceIndices.containsKey(instances.get(i)
//							.getSource()),
//							"Something is wrong. All instances must be in the instance index map.");
//		}
//
//		// extract labels (in order defined by instanceIndices)
//		int[] gold = IntArrays.repeat(-1, instances.size());
//		for (Instance<Integer, SparseFeatureVector> inst : instances) {
//			gold[instanceIndices.get(inst.getSource())] = inst.getLabel();
//		}
//
//		return gold;
//	}
//
//	/**
//	 * Creates a map from an instance (identity) to that instance's index in the
//	 * dataset. This is mainly useful for mapping multi-annotator model
//	 * predictions back to instances that may be presented in a different order
//	 * than that assumed by the model. Because the multiannotators are
//	 * transductive models, we infer labels only for instances that we
//	 * specifically ran inference on. Of course, model parameters MAY be used to
//	 * generate predictions for unseen instances based on feature values, but
//	 * that is a post-hoc process that is different from directly inferring a
//	 * label.
//	 */
//	public static Map<String, Integer> instanceIndices(Dataset data) {
//		Map<String, Integer> map = Maps.newHashMap();
//		int i = 0;
//		for (Instance<Integer, SparseFeatureVector> instance : data
//				.allInstances()) {
//			Preconditions
//					.checkState(
//							!map.containsKey(instance.getSource()),
//							"Dataset contains the same instance (same instance source) twice. This is not allowed!");
//			map.put(instance.getSource(), i++);
//		}
//		return map;
//	}
//
//	public static Dataset removeUnannotatedData(Dataset data) {
//		// all labeled items come along. Labels are gold truth (as opposed to
//		// annotations)
//		Collection<Instance<Integer, SparseFeatureVector>> labeledData = Lists
//				.newArrayList(data.labeledData());
//		// only unlabeled instances with annotations come along
//		Collection<Instance<Integer, SparseFeatureVector>> unlabeledData = Lists
//				.newArrayList();
//		for (Instance<Integer, SparseFeatureVector> item : data
//				.unlabeledInstances()) {
//			if (item.getAnnotations().size() > 0) {
//				unlabeledData.add(item);
//			}
//		}
//		return new Dataset(labeledData, unlabeledData, data.getWordIndex(),
//				data.getLabelIndex());
//	}
//
//	/**
//	 * Remove documents from the collection with duplicate sources. In all cases
//	 * keep only the first item.
//	 */
//	public static Dataset removeDuplicateSources(Dataset data) {
//		Set<String> docSources = Sets.newHashSet();
//
//		// labeled
//		Collection<Instance<Integer, SparseFeatureVector>> labeledData = Lists
//				.newArrayList();
//		for (Instance<Integer, SparseFeatureVector> item : data.labeledData()) {
//			if (docSources.contains(item.getSource())) {
//				logger.warning("Repeated data item " + item.getSource());
//			} else {
//				labeledData.add(item);
//				docSources.add(item.getSource());
//			}
//		}
//		// unlabeled instances
//		Collection<Instance<Integer, SparseFeatureVector>> unlabeledData = Lists
//				.newArrayList();
//		for (Instance<Integer, SparseFeatureVector> item : data
//				.unlabeledInstances()) {
//			if (docSources.contains(item.getSource())) {
//				logger.warning("Repeated data item " + item.getSource());
//			} else {
//				unlabeledData.add(item);
//				docSources.add(item.getSource());
//			}
//		}
//		return new Dataset(labeledData, unlabeledData, data.getWordIndex(),
//				data.getLabelIndex());
//	}
//
//	/**
//	 * @param trainingData
//	 */
//	public static Dataset removeAnnotations(Dataset data) {
//
//		Collection<Instance<Integer, SparseFeatureVector>> labeledData = Lists
//				.newArrayList();
//		for (Instance<Integer, SparseFeatureVector> inst : data.labeledData()) {
//			Instance<Integer, SparseFeatureVector> bareinst = BasicInstance.of(
//					inst.getLabel(), inst.isLabelObserved(), inst.getSource(),
//					inst.getData());
//			labeledData.add(bareinst);
//		}
//
//		Collection<Instance<Integer, SparseFeatureVector>> unlabeledData = Lists
//				.newArrayList();
//		for (Instance<Integer, SparseFeatureVector> inst : data
//				.unlabeledInstances()) {
//			Instance<Integer, SparseFeatureVector> bareinst = BasicInstance.of(
//					inst.getLabel(), inst.isLabelObserved(), inst.getSource(),
//					inst.getData());
//			unlabeledData.add(bareinst);
//		}
//
//		return new Dataset(labeledData, unlabeledData, data.getWordIndex(),
//				data.getLabelIndex());
//	}
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
//	/**
//	 * Move all but N labels per annotator (if that many labeled annotations per
//	 * annotator exist) from labeledData into unlabeledInstances. Greedy; no
//	 * optimality guarantee.
//	 * 
//	 * N.B. This method is EXTREMELY inefficient!
//	 */
//	public static Dataset hideAllLabelsButNPerAnnotator(Dataset data,
//			int numObservedLabelsPerAnnotator) {
//		int numAnnotators = getAnnotators(data).size();
//		Collection<Instance<Integer, SparseFeatureVector>> unlabeled = Lists
//				.newArrayList();
//		// include labeled instances (we'll move these to the labeled set
//		// incrementally)
//		unlabeled.addAll(data.labeledData());
//
//		Collection<Instance<Integer, SparseFeatureVector>> labeled = Lists
//				.newArrayList();
//		Multiset<Long> annotatorCounts = HashMultiset.create();
//
//		// greedily choose a set of labeled data such that at least
//		// K=numObservedLabelsPerAnnotator
//		// instances have been annotated per annotator.
//		while (annotatorCounts.elementSet().size() < numAnnotators
//				|| annotatorCounts.count(Multisets2.minCount(annotatorCounts)) < numObservedLabelsPerAnnotator) {
//			// find instance with the most needed annotators to contribute
//			Instance<Integer, SparseFeatureVector> argmax = null;
//			int max = 0; // only consider adding items w/ positive contributions
//			for (Instance<Integer, SparseFeatureVector> inst : unlabeled) {
//				// aggregate annotators for this instance
//				Multiset<Long> instanceAnnotatorCounts = HashMultiset.create();
//				for (Entry<Long, TimedAnnotation<Integer>> ann : inst
//						.getAnnotations().entries()) {
//					instanceAnnotatorCounts.add(ann.getKey());
//				}
//				// calculate useful contributions for this instance
//				int numContributions = 0;
//				for (Long annotator : instanceAnnotatorCounts.elementSet()) {
//					// only count contributions that are going to get us closer
//					// to fulfilling numObservedLabelsPerAnnotator
//					int maxContrib = Math.max(0, numObservedLabelsPerAnnotator
//							- annotatorCounts.count(annotator));
//					numContributions += Math.min(maxContrib,
//							instanceAnnotatorCounts.count(annotator));
//				}
//				if (numContributions > max) {
//					argmax = inst;
//					max = numContributions;
//				}
//			}
//			// found nothing that will contribute (impossible goal given data)
//			if (argmax == null) {
//				logger.warning("Unable to find "
//						+ numObservedLabelsPerAnnotator
//						+ " labeled instances for each annotator. Stopping early\n\t"
//						+ annotatorCounts);
//				break;
//			}
//			// move from unlabeled to labeled data
//			else {
//				unlabeled.remove(argmax);
//				labeled.add(argmax);
//				for (Entry<Long, TimedAnnotation<Integer>> ann : argmax
//						.getAnnotations().entries()) {
//					annotatorCounts.add(ann.getKey());
//				}
//			}
//		}
//		unlabeled.addAll(data.unlabeledInstances()); // include unlabeled
//														// instances
//		return new Dataset(labeled, unlabeled, data.getWordIndex(),
//				data.getLabelIndex());
//	}
//
//	/**
//	 * Move all but N labels per class (if that many labeled annotations per
//	 * class exist) from labeledData into unlabeledInstances.
//	 */
//	public static Dataset hideAllLabelsButNPerClass(Dataset data,
//			int numObservedLabelsPerClass, RandomGenerator rnd) {
//		int numClasses = data.getNumLabels();
//		List<Instance<Integer, SparseFeatureVector>> unlabeled = Lists
//				.newArrayList();
//		unlabeled.addAll(data.labeledData()); // include labeled instances (for
//												// now)
//
//		Collection<Instance<Integer, SparseFeatureVector>> labeled = Lists
//				.newArrayList();
//		Multiset<Integer> classCounts = HashMultiset.create();
//
//		// greedily choose a set of labeled data such that at least
//		// K=numObservedLabelsPerAnnotator
//		// instances have been annotated per annotator.
//		while (classCounts.elementSet().size() < numClasses
//				|| Multisets2.minCount(classCounts) < numObservedLabelsPerClass) {
//
//			// assemble a list of instances that have a class we need AND have
//			// at least one annotation
//			List<Instance<Integer, SparseFeatureVector>> candidates = Lists
//					.newArrayList();
//			for (Instance<Integer, SparseFeatureVector> cand : unlabeled) {
//				if (classCounts.count(cand.getLabel()) < numObservedLabelsPerClass
//						&& cand.getAnnotations().size() > 0) {
//					candidates.add(cand);
//				}
//			}
//			// if we have to, add items that don't have any annotations
//			if (candidates.size() == 0) {
//				for (Instance<Integer, SparseFeatureVector> cand : unlabeled) {
//					if (classCounts.count(cand.getLabel()) < numObservedLabelsPerClass) {
//						candidates.add(cand);
//					}
//				}
//			}
//			// found nothing that will contribute (impossible goal given data)
//			if (candidates.size() == 0) {
//				logger.warning("Unable to find "
//						+ numObservedLabelsPerClass
//						+ " labeled instances for each class. Stopping early\n\t"
//						+ classCounts);
//				break;
//			}
//
//			// choose at random among candidates
//			Instance<Integer, SparseFeatureVector> chosen = candidates.get(rnd
//					.nextInt(candidates.size()));
//			unlabeled.remove(chosen);
//			labeled.add(chosen);
//			classCounts.add(chosen.getLabel());
//		}
//		unlabeled.addAll(data.unlabeledInstances()); // include originally
//														// unlabeled instances
//		return new Dataset(labeled, unlabeled, data.getWordIndex(),
//				data.getLabelIndex());
//
//	}
//
//	public static Dataset hideAllLabelsButEmpiricallyObserved(Dataset data) {
//		List<Instance<Integer, SparseFeatureVector>> labeled = Lists
//				.newArrayList();
//		List<Instance<Integer, SparseFeatureVector>> unlabeled = Lists
//				.newArrayList();
//
//		for (Instance<Integer, SparseFeatureVector> inst : data.labeledData()) {
//			if (inst.isLabelObserved()) {
//				labeled.add(inst);
//			} else {
//				unlabeled.add(inst);
//			}
//		}
//
//		unlabeled.addAll(data.unlabeledInstances()); // include originally
//														// unlabeled instances
//		return new Dataset(labeled, unlabeled, data.getWordIndex(),
//				data.getLabelIndex());
//	}
//
//	/**
//	 * convert a dataset to the following simplified array form:
//	 * result=double[num_instances][num_features] so that result[i][f] returns
//	 * the count of feature f in document i.
//	 * 
//	 * Note: all side information such as annotations, labels, and data source
//	 * is lost.
//	 */
//	public static double[][] toFeatureArray(Dataset data) {
//		double[][] countOfXandF = new double[data.allInstances().size()][];
//		int docIndex = 0;
//		for (Instance<Integer, SparseFeatureVector> instance : data
//				.allInstances()) {
//			countOfXandF[docIndex] = toFeatureArray(instance,
//					data.getNumFeatures());
//			++docIndex;
//		}
//		return countOfXandF;
//	}
//
//	public static double[] toFeatureArray(
//			Instance<Integer, SparseFeatureVector> instance, int numFeatures) {
//		double[] countOfXandF = new double[numFeatures];
//		instance.getData().addTo(countOfXandF);
//		return countOfXandF;
//	}
//
//	public static List<Map<Integer, Double>> toSparseFeatureArray(Dataset data) {
//		List<Map<Integer, Double>> retval = Lists.newArrayList();
//		for (Instance<Integer, SparseFeatureVector> inst : data.allInstances()) {
//			retval.add(toSparseFeatureArray(inst));
//		}
//		return retval;
//	}
//
//	public static Map<Integer, Double> toSparseFeatureArray(
//			Instance<Integer, SparseFeatureVector> instance) {
//		final Map<Integer, Double> sparseFeatures = Maps.newHashMap();
//		instance.getData().visitSparseEntries(new EntryVisitor() {
//			@Override
//			public void visitEntry(int index, double value) {
//				sparseFeatures.put(index, value);
//			}
//		});
//		return sparseFeatures;
//	}
//
//	/**
//	 * convert annotations in a dataset into an int tensor
//	 * a[num_instances][num_annotators][num_classes] where entry a[i][j][k] is
//	 * the number of times annotations annotator j annotatoted instance i with
//	 * class k.
//	 */
//	public static int[][][] annotations(
//			Iterable<Instance<Integer, SparseFeatureVector>> instances,
//			int numInstances, int numLabels, int numAnnotators) {
//		int[][][] a = new int[numInstances][0][];
//		for (Enumeration<Instance<Integer, SparseFeatureVector>> e : Iterables2
//				.enumerate(instances)) {
//			a[e.getIndex()] = annotations(e.getElement(), numLabels,
//					numAnnotators);
//		}
//		return a;
//	}
//
//	public static int[][] annotations(
//			Instance<Integer, SparseFeatureVector> instance, int numLabels,
//			int numAnnotators) {
//		int[][] annotations = new int[numAnnotators][numLabels];
//		Collection<Entry<Long, TimedAnnotation<Integer>>> entries = instance
//				.getAnnotations().entries();
//		for (Entry<Long, TimedAnnotation<Integer>> entry : entries) {
//			annotations[entry.getKey().intValue()][entry.getValue()
//					.getAnnotation()] += 1;
//		}
//		return annotations;
//	}
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
	 */
	public static Dataset convert(
			String datasetSource,
			Iterable<FlatInstance<SparseFeatureVector, Integer>> labeledInstances,
			Indexer<String> featureIndex, Indexer<String> labelIndex, 
			Indexer<Long> instanceIdIndex, Indexer<Long> annotatorIdIndex) {
		
		TableCounter<Long, Long, Integer> annotationCounter = TableCounter.create();
		Set<Long> instanceIndices = Sets.newHashSet();
		Map<Long,Integer> labelMap = Maps.newHashMap();
		Map<Long,String> sourceMap = Maps.newHashMap();
		Map<Long,SparseFeatureVector> featureMap = Maps.newHashMap();
		int numTokens = 0;
		
		// pre-calculate quantities 
		for (FlatInstance<SparseFeatureVector, Integer> inst: labeledInstances){
			// anotations
			if (inst.isAnnotation()){
				long instanceId = inst.getInstanceId();
				long annotatorId = inst.getAnnotator();
				
				instanceIndices.add(instanceId);
				// record instance features (in case this is a previously-unseen instance)
				if (inst.getData()!=null){
					featureMap.put(instanceId, inst.getData());
				}
				// record instance source
				if (inst.getSource()!=null){
					sourceMap.put(instanceId, inst.getSource());
				}
				// record annotation
				annotationCounter.incrementCount(instanceId, annotatorId, inst.getLabel());
			}
			// labels
			else{
				long instanceId = inst.getInstanceId();
				
				instanceIndices.add(instanceId);
				// record label
				labelMap.put(instanceId, inst.getLabel());
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
			numTokens += featureMap.get(instanceIndex).sum();
			
			final AnnotationSet annotationSet = BasicAnnotationSet.fromCountTable(
					instanceIndex, annotatorIdIndex.size(), labelIndex.size(), annotationCounter);
			
			instances.add(new BasicDatasetInstance(
					featureMap.get(instanceIndex), 
					labelMap.get(instanceIndex), 
					null, // regressand
					annotationSet , 
					sourceMap.get(instanceIndex),
					labelIndex));
		}
		
		// annotations
		return new BasicDataset(datasetSource, instances, numTokens, annotatorIdIndex, featureIndex, labelIndex, instanceIdIndex);
	}

	public static Pair<? extends Dataset, ? extends Dataset> divideLabeledFromUnlabeled(Dataset dataset){
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
		
		return Pair.of(new BasicDataset(labeledData, recalulateInfo(labeledData, dataset.getInfo())),
				new BasicDataset(unlabeledData, recalulateInfo(unlabeledData, dataset.getInfo())));
	}
	
	public static DatasetInfo recalulateInfo(List<DatasetInstance> instances, DatasetInfo previousInfo){
		return new BasicDataset.Info(
				previousInfo.getSource(), 
				instances.size(), 
				numTokens(instances), 
				previousInfo.getAnnotatorIdIndexer(), 
				previousInfo.getFeatureIndexer(), 
				previousInfo.getLabelIndexer(), 
				previousInfo.getInstanceIdIndexer());
	}

	public static int numTokens(List<DatasetInstance> instances){
		int total = 0;
		for (DatasetInstance inst: instances){
			total += inst.asFeatureVector().sum();
		}
		return total;
	}
	
}
