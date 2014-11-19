/**
 * 
 */
package edu.byu.nlp.data.pipes;


import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import edu.byu.nlp.data.FlatInstance;

/**
 * @author robbie
 * @author plf1
 *
 */
public class LabeledInstancePipeFactories {
	
	private LabeledInstancePipeFactories() { }
	
	public static <ID, IL, OD, OL> PipeFactory<ID, IL, OD, OL> transformingPipeFactory(
			final Function<FlatInstance<ID, IL>, FlatInstance<OD, OL>> labelF) {
		return new AbstractStatelessPipeFactory<ID, IL, OD, OL>() {
			@Override
			protected LabeledInstancePipe<ID, IL, OD, OL> createLabeledInstancePipe() {
				return Pipes.labeledInstanceTransformingPipe(labelF);
			}
		};
	}
	
//	public static <ID, OD, L> PipeFactory<ID, L, OD, L> dataTransformingPipeFactory(final Function<ID, OD> dataF) {
//		return transformingPipeFactory(
//				Instances.transformedLabeledInstanceFunction(dataF, Functions.<L>identity(), Functions.<String>identity()));
//	}
//	
//	public static <D, IL, OL> PipeFactory<D, IL, D, OL> labelTransformingPipeFactory(final Function<IL, OL> labelF) {
//		return transformingPipeFactory(
//				Instances.transformedLabeledInstanceFunction(Functions.<D>identity(), labelF, Functions.<String>identity()));
//	}
//
//	public static <D, L> PipeFactory<D, L, D, L> sourceTransformingPipeFactory(final Function<String, String> sourceF) {
//		return transformingPipeFactory(
//				Instances.transformedLabeledInstanceFunction(Functions.<D>identity(), Functions.<L>identity(), sourceF));
//	}
	
	public static <D, L> PipeFactory<D, L, D, L> filteringPipeFactory(final Predicate<FlatInstance<D, L>> predicate) {
		return new AbstractStatelessPipeFactory<D, L, D, L>() {
			@Override
			protected LabeledInstancePipe<D, L, D, L> createLabeledInstancePipe() {
				return Pipes.labeledInstanceFilteringPipe(predicate);
			}
		};
	}
	
//	public static <D, L> PipeFactory<D, L, D, L> dataFilteringPipeFactory(final Predicate<D> dataP) {
//		return filteringPipeFactory(
//				Instances.<D, L>labeledInstancePredicate(dataP, Predicates.<L>alwaysTrue(), Predicates.<String>alwaysTrue()));
//	}
//	
//	public static <D, L> PipeFactory<D, L, D, L> labelFilteringPipeFactory(final Predicate<L> labelP) {
//		return filteringPipeFactory(
//				Instances.<D, L>labeledInstancePredicate(Predicates.<D>alwaysTrue(), labelP, Predicates.<String>alwaysTrue()));
//	}
//
//	public static <D, L> PipeFactory<D, L, D, L> sourceFilteringPipeFactory(final Predicate<String> sourceP) {
//		return filteringPipeFactory(
//				Instances.<D, L>labeledInstancePredicate(Predicates.<D>alwaysTrue(), Predicates.<L>alwaysTrue(), sourceP));
//	}
//
//	public static <ID, OD, L> PipeFactory<ID, L, OD, L> oneToMany(final OneToManyLabeledInstanceFunction<ID, OD> dataF) {
//		return new AbstractStatelessPipeFactory<ID, L, OD, L>() {
//			@Override
//			protected LabeledInstancePipe<ID, L, OD, L> createLabeledInstancePipe() {
//				return Pipes.oneToManyLabeledInstancePipe(dataF);
//			}
//			@Override
//			protected AnnotatedInstancePipe<ID, L, OD, L> createAnnotatedInstancePipe() {
//				throw new UnsupportedOperationException();
//			}
//		};
//	}

}
