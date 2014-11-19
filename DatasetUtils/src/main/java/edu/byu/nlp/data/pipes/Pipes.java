/**
 * 
 */
package edu.byu.nlp.data.pipes;

import java.util.Iterator;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

import edu.byu.nlp.data.FlatInstance;
import edu.byu.nlp.data.pipes.Instances.OneToManyLabeledInstanceFunction;
import edu.byu.nlp.io.AbstractIterable;
import edu.byu.nlp.util.Iterators2;

/**
 * @author robbie
 * @author pfelt
 *
 */
public class Pipes {

	//-----------------------------------------------------------
	// Labeled Instance Transform
	//-----------------------------------------------------------
	public static <ID, IL, OD, OL> LabeledInstancePipe<ID, IL, OD, OL> labeledInstanceTransformingPipe(
			final Function<FlatInstance<ID, IL>, FlatInstance<OD, OL>> f) {
		
		return new LabeledInstancePipe<ID, IL, OD, OL>() {
			@Override
			public Iterable<FlatInstance<OD, OL>> apply(final Iterable<FlatInstance<ID, IL>> instances) {
				return Iterables.transform(instances, f);
			}
		};
	}

	public static <ID, IL, OD, OL> LabeledInstancePipe<ID, IL, OD, OL> labeledInstanceTransformingPipe(
			Function<ID, OD> dataF, Function<IL, OL> labelF, Function<String, String> sourceF, Function<Long,Long> instanceIdF, Function<Long,Long> annotatorIdF) {
		return labeledInstanceTransformingPipe(Instances.<ID,IL,OD,OL>transformedLabeledInstanceFunction(dataF, labelF, sourceF, instanceIdF, annotatorIdF));
	}
	
	public static <ID, OD, L> LabeledInstancePipe<ID, L, OD, L> labeledInstanceDataTransformingPipe(Function<ID, OD> dataF) {
		return labeledInstanceTransformingPipe(dataF, Functions.<L>identity(), Functions.<String>identity(), Functions.<Long>identity(), Functions.<Long>identity());
	}

	public static <D, IL, OL> LabeledInstancePipe<D,IL, D, OL> labeledInstanceLabelTransformingPipe(Function<IL, OL> labelF) {
		return labeledInstanceTransformingPipe(Functions.<D>identity(), labelF, Functions.<String>identity(), Functions.<Long>identity(), Functions.<Long>identity());
	}

	public static <D, L> LabeledInstancePipe<D, L, D, L> labeledInstanceSourceTransformingPipe(Function<String, String> sourceF) {
		return labeledInstanceTransformingPipe(Functions.<D>identity(), Functions.<L>identity(), sourceF, Functions.<Long>identity(), Functions.<Long>identity());
	}

	public static <D, L> LabeledInstancePipe<D, L, D, L> labeledInstanceInstanceIdTransformingPipe(Function<Long, Long> instanceIdF) {
		return labeledInstanceTransformingPipe(Functions.<D>identity(), Functions.<L>identity(), Functions.<String>identity(), instanceIdF, Functions.<Long>identity());
	}
	
	public static <D, L> LabeledInstancePipe<D, L, D, L> labeledInstanceAnnotatorIdTransformingPipe(Function<Long, Long> annotatorIdF) {
		return labeledInstanceTransformingPipe(Functions.<D>identity(), Functions.<L>identity(), Functions.<String>identity(), Functions.<Long>identity(), annotatorIdF);
	}

	


//	//-----------------------------------------------------------
//	// Annotated Instance Transform
//	//-----------------------------------------------------------
//	public static <ID, IL, OD, OL> AnnotatedInstancePipe<ID, IL, OD, OL> annotatedInstanceTransformingPipe(
//			final Function<Annotation<ID, IL>, Annotation<OD, OL>> f) {
//		
//		return new AnnotatedInstancePipe<ID, IL, OD, OL>() {
//			@Override
//			public Iterable<Annotation<OD, OL>> apply(final Iterable<Annotation<ID, IL>> instances) {
//				return Iterables.transform(instances, f);
//			}
//		};
//	}
//
//	public static <ID, IL, OD, OL> AnnotatedInstancePipe<ID, IL, OD, OL> annotatedInstanceTransformingPipe(Function<ID, OD> dataF, Function<IL, OL> labelF, Function<String, String> sourceF) {
//		return annotatedInstanceTransformingPipe(Instances.<ID,IL,OD,OL>transformedAnnotatedInstanceFunction(dataF, labelF, sourceF));
//	}
//	
//	public static <ID, OD, L> AnnotatedInstancePipe<ID, L, OD, L> annotatedInstanceDataTransformingPipe(Function<ID, OD> dataF) {
//		return annotatedInstanceTransformingPipe(dataF, Functions.<L>identity(), Functions.<String>identity());
//	}
//
//	public static <D, IL, OL> AnnotatedInstancePipe<D,IL, D, OL> annotatedInstanceLabelTransformingPipe(Function<IL, OL> labelF) {
//		return annotatedInstanceTransformingPipe(Functions.<D>identity(), labelF, Functions.<String>identity());
//	}
//
//	public static <D, L> AnnotatedInstancePipe<D, L, D, L> annotatedInstanceSourceTransformingPipe(Function<String, String> sourceF) {
//		return annotatedInstanceTransformingPipe(Functions.<D>identity(), Functions.<L>identity(), sourceF);
//	}

	

	//-----------------------------------------------------------
	// Labeled Instance Filter
	//-----------------------------------------------------------
	public static <D, L> LabeledInstancePipe<D, L, D, L> labeledInstanceFilteringPipe(final Predicate<FlatInstance<D, L>> p) {
		return new LabeledInstancePipe<D, L, D, L>() {
			@Override
			public Iterable<FlatInstance<D, L>> apply(Iterable<FlatInstance<D, L>> instances) {
				return Iterables.filter(instances, p);
			}
		};
	}
	
	public static <D, L> LabeledInstancePipe<D, L, D, L> labeledInstanceFilteringPipe(final Predicate<D> dataP, final Predicate<L> labelP, final Predicate<String> sourceP) {
		return labeledInstanceFilteringPipe(Instances.<D,L>labeledInstancePredicate(dataP, labelP, sourceP));
	}

	public static <D, L> LabeledInstancePipe<D, L, D, L> labeledInstanceDataFilteringPipe(final Predicate<D> dataP) {
		return labeledInstanceFilteringPipe(dataP,Predicates.<L>alwaysTrue(),Predicates.<String>alwaysTrue());
	}

	public static <D, L> LabeledInstancePipe<D, L, D, L> labeledInstanceLabelFilteringPipe(final Predicate<L> labelP) {
		return labeledInstanceFilteringPipe(Predicates.<D>alwaysTrue(),labelP,Predicates.<String>alwaysTrue());
	}

	public static <D, L> LabeledInstancePipe<D, L, D, L> labeledInstanceSourceFilteringPipe(final Predicate<String> sourceP) {
		return labeledInstanceFilteringPipe(Predicates.<D>alwaysTrue(),Predicates.<L>alwaysTrue(),sourceP);
	}
	

//	//-----------------------------------------------------------
//	// Annotated Instance Filter
//	//-----------------------------------------------------------
//	public static <D, L> AnnotatedInstancePipe<D, L, D, L> annotatedInstanceFilteringPipe(final Predicate<D> dataP, final Predicate<L> labelP, final Predicate<String> sourceP) {
//		return new AnnotatedInstancePipe<D, L, D, L>() {
//			@Override
//			public Iterable<Annotation<D, L>> apply(Iterable<Annotation<D, L>> instances) {
//				return Iterables.filter(instances, Instances.<D,L>annotatedInstancePredicate(dataP, labelP, sourceP));
//			}
//		};
//	}
//
//	public static <D, L> AnnotatedInstancePipe<D, L, D, L> annotatedInstanceDataFilteringPipe(final Predicate<D> dataP) {
//		return annotatedInstanceFilteringPipe(dataP,Predicates.<L>alwaysTrue(),Predicates.<String>alwaysTrue());
//	}
//
//	public static <D, L> AnnotatedInstancePipe<D, L, D, L> annotatedInstanceLabelFilteringPipe(final Predicate<L> labelP) {
//		return annotatedInstanceFilteringPipe(Predicates.<D>alwaysTrue(),labelP,Predicates.<String>alwaysTrue());
//	}
//
//	public static <D, L> AnnotatedInstancePipe<D, L, D, L> annotatedInstanceSourceFilteringPipe(final Predicate<String> sourceP) {
//		return annotatedInstanceFilteringPipe(Predicates.<D>alwaysTrue(),Predicates.<L>alwaysTrue(),sourceP);
//	}


	public static <ID, OD, L> LabeledInstancePipe<ID, L, OD, L> oneToManyLabeledInstancePipe(final OneToManyLabeledInstanceFunction<ID, OD, L> dataF){
		return new LabeledInstancePipe<ID, L, OD, L>() {

			@Override
			public Iterable<FlatInstance<OD, L>> apply(final Iterable<FlatInstance<ID, L>> instances) {
				return new AbstractIterable<FlatInstance<OD,L>>(){
					@Override
					public Iterator<FlatInstance<OD, L>> iterator() {
						return Iterators2.lazyConcatenate(Iterables.transform(instances, dataF));
//						return Iterators2.lazyConcatenate(Iterables.transform(instances, Instances.<ID,OD,L>oneToManyDataFunction(dataF)));
					}
				};
			}
			
		};
	}
	

	public static <D, L> LabeledInstancePipe<D, L, D, L> passThrough() {
		return new LabeledInstancePipe<D, L, D, L>() {
			@Override
			public Iterable<FlatInstance<D,L>> apply(Iterable<FlatInstance<D,L>> instances) {
				return instances;
			}
		};
	}

	public static <ID, IL, OD, OL, O> O apply(DataSource<ID, IL> src, LabeledInstancePipe<ID, IL, OD, OL> pipe,
			DataSink<OD, OL, O> sink) {
		return sink.processLabeledInstances(pipe.apply(src.getLabeledInstances()));
	}

//	public static <ID, IL, OD, OL, O> O apply(DataSource<ID, IL> src, AnnotatedInstancePipe<ID, IL, OD, OL> pipe,
//			DataSink<OD, OL, O> sink) {
//		return sink.processAnnotatedInstances(pipe.apply(src.getAnnotatedInstances()));
//	}

}
