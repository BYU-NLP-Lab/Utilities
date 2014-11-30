/**
 * 
 */
package edu.byu.nlp.data.pipes;

import java.util.Iterator;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import edu.byu.nlp.annotationinterface.Instance;
import edu.byu.nlp.data.AbstractFlatInstance;
import edu.byu.nlp.data.FlatInstance;
import edu.byu.nlp.data.types.DatasetInstance;

/**
 * @author robbie
 * @author plf1
 *
 */
public class Instances {
	
	private Instances() {}
	
	
//	//-------------------------------------------------------------
//	// transforming instances
//	//-------------------------------------------------------------
//	private static abstract class AbstractDataTransformedInstance<ID, OD> extends BasicInstance<OD> {
//
//		private Instance<ID> instance;
//
//		public AbstractDataTransformedInstance(Instance<ID> instance) {
//			super(instance.getId(), null);
//			this.instance=instance;
//		}
//		
//		@Override
//		public String getSource() {
//			return transformedSource(instance.getSource());
//		}
//		
//		@Override
//		public OD getValue() {
//			return transformedValue(instance.getValue());
//		}
//
//		protected abstract OD transformedValue(ID value);
//		
//		protected abstract String transformedSource(String value);
//	}
//	
//	public interface TransformedInstanceFunction<ID, OD> extends Function<Instance<ID>, Instance<OD>> {
//	}
//	
//	public static <ID, OD> TransformedInstanceFunction<ID, OD> transformedInstanceFunction(
//			final Function<? super ID, OD> dataF, final Function<String,String> sourceF) {
//		
//		return new TransformedInstanceFunction<ID, OD>() {
//
//			@Override
//			public Instance<OD> apply(Instance<ID> from) {
//				return new AbstractDataTransformedInstance<ID, OD>(from) {
//
//					@Override
//					protected OD transformedValue(ID value) {
//						return dataF.apply(value);
//					}
//
//					@Override
//					protected String transformedSource(String value) {
//						return sourceF.apply(value);
//					}
//
//				};
//			}
//			
//		};
//	}
//	
//	public static <ID, OD> Instance<OD> transformedInstance(final Instance<ID> instance, final Function<ID, OD> dataF, Function<String,String> sourceF) {
//		return Instances.<ID, OD>transformedInstanceFunction(dataF,sourceF).apply(instance);
//	}

	
	

	//-------------------------------------------------------------
	// Transforming Labeled Instances
	//-------------------------------------------------------------
	public interface TransformedLabeledInstanceFunction<ID, IL, OD, OL> extends Function<FlatInstance<ID, IL>, FlatInstance<OD, OL>> {
	}
	
	private static abstract class AbstractTransformedFlatInstance<ID, IL, OD, OL> extends AbstractFlatInstance<OD,OL> {
		private FlatInstance<ID,IL> delegate;
		public AbstractTransformedFlatInstance(FlatInstance<ID,IL> delegate){
			this.delegate=delegate;
		}
		@Override
		public String getSource() {
			return transformedInstanceSource(delegate.getSource());
		}
		@Override
		public OD getData() {
			return transformedInstanceValue(delegate.getData());
		}
		@Override
		public long getInstanceId() {
			return transformInstanceId(delegate.getInstanceId());
		}
		@Override
		public OL getLabel() {
			return transformedLabelValue(delegate.getLabel());
		}
		@Override
		public boolean isAnnotation() {
			return delegate.isAnnotation();
		}
		@Override
		public long getStartTimestamp() {
			return delegate.getStartTimestamp();
		}
		@Override
		public long getEndTimestamp() {
			return delegate.getEndTimestamp();
		}
		@Override
		public long getAnnotator() {
			return transformAnnotatorId(delegate.getAnnotator());
		}
		@Override
		public OL getAutomaticLabel() {
			return transformedLabelValue(delegate.getAutomaticLabel());
		}
		@Override
		public long getAutomaticLabelerId() {
			return delegate.getAutomaticLabelerId();
		}
		
		protected abstract OL transformedLabelValue(IL value);
		protected abstract OD transformedInstanceValue(ID value);
		protected abstract String transformedInstanceSource(String value);
		protected abstract Long transformInstanceId(Long value);
		protected abstract Long transformAnnotatorId(Long value);
	}
	
	
	
	public static <ID, IL, OD, OL> TransformedLabeledInstanceFunction<ID, IL, OD, OL> transformedLabeledInstanceFunction(
			final Function<ID,OD> dataF, final Function<IL,OL> labelF, final Function<String,String> sourceF,
			final Function<Long,Long> instanceIdF, final Function<Long,Long> annotatorIdF){
		return new TransformedLabeledInstanceFunction<ID, IL, OD, OL>() {
			@Override
			public FlatInstance<OD, OL> apply(FlatInstance<ID, IL> lab) {
				return new AbstractTransformedFlatInstance<ID,IL,OD,OL>(lab){
					@Override
					protected OL transformedLabelValue(IL value) {
						return labelF.apply(value);
					}
					@Override
					protected OD transformedInstanceValue(ID value) {
						return (value==null)? null: dataF.apply(value);
					}
					@Override
					protected String transformedInstanceSource(String value) {
						return sourceF.apply(value);
					}
					@Override
					protected Long transformInstanceId(Long value) {
						return instanceIdF.apply(value);
					}
					@Override
					protected Long transformAnnotatorId(Long value) {
						return annotatorIdF.apply(value);
					}
				};
			}
		};
	}

	public static <ID, IL, OD, OL> FlatInstance<OD,OL> transformedLabeledInstance(
			FlatInstance<ID, IL> label, 
			Function<ID,OD> dataF, Function<IL,OL> labelF, Function<String,String> sourceF,
			Function<Long,Long> instanceIdF, Function<Long,Long> annotatorIdF){
		return Instances.<ID, IL, OD, OL>transformedLabeledInstanceFunction(dataF,labelF,sourceF,instanceIdF,annotatorIdF).apply(label);
	}
	
	

//	//-------------------------------------------------------------
//	// transforming annotated instances
//	//-------------------------------------------------------------
//	public interface TransformedAnnotatedInstanceFunction<ID, IL, OD, OL> extends Function<Annotation<ID,IL>, Annotation<OD,OL>> {
//	}
//	
//	private static abstract class AbstractTransformedAnnotationInstance<ID, IL, OD, OL> extends BasicJavaAnnotationInstance<OD, OL>{
//		private Instance<ID> inputInstance;
//
//		public AbstractTransformedAnnotationInstance(long aiId,
//				Annotator annotator, Instance<ID> instance,
//				FlatInstance<ID, IL> preAnnotation,
//				List<TimelineEvent> timelineEvents) {
//			super(aiId, annotator, null, null, timelineEvents);
//			this.inputInstance=instance;
//		}
//
//		@Override
//		public Instance<OD> getInstance() {
//			return new AbstractDataTransformedInstance<ID, OD>(inputInstance){
//				@Override
//				protected OD transformedValue(ID value) {
//					return transformedInstanceValue(value);
//				}
//				@Override
//				protected String transformedSource(String value) {
//					return transformedInstanceSource(value);
//				}
//			};
//		}
//		
//		@Override
//		public FlatInstance<OD, OL> getFlatInstance() {
//			throw new UnsupportedOperationException();
//		}
//
//		protected abstract String transformedInstanceSource(String value);
//		protected abstract OD transformedInstanceValue(ID value);
//	}
//	
//	private static abstract class AbstractTransformedAnnotatedInstance<ID, IL, OD, OL> extends BasicAnnotation<OD, OL> {
//
//		private Annotation<ID, IL> annotation;
//
//		public AbstractTransformedAnnotatedInstance(Annotation<ID, IL> annotation) {
//			super(null, null);
//			this.annotation=annotation;
//		}
//		
//		@Override
//		public OL getValue() {
//			return transformedAIAnnotationValue(annotation.getValue());
//		}
//		
//		@Override
//		public JavaAnnotationInstance<OD, OL> getAnnotationInstance() {
//			return new AbstractTransformedAnnotationInstance<ID,IL,OD,OL>(annotation.getAnnotationInstance().getId(),
//					annotation.getAnnotationInstance().getAnnotator(), annotation.getAnnotationInstance().getInstance(), 
//					annotation.getAnnotationInstance().getFlatInstance(), annotation.getAnnotationInstance().getTimelineEvents()){
//						@Override
//						protected OD transformedInstanceValue(ID value) {
//							return transformedAIInstanceValue(value);
//						}
//						@Override
//						protected String transformedInstanceSource(String value) {
//							return transformedAISourceValue(value);
//						}
//			};
//		}
//
//		protected abstract String transformedAISourceValue(String value);
//		
//		protected abstract OL transformedAIAnnotationValue(IL value);
//		
//		protected abstract OD transformedAIInstanceValue(ID value);
//		
//	}
//
//	public static <ID, IL, OD, OL> TransformedAnnotatedInstanceFunction<ID, IL, OD, OL> transformedAnnotatedInstanceFunction(
//			final Function<ID,OD> dataF, final Function<IL,OL> annF, final Function<String,String> sourceF){
//		return new TransformedAnnotatedInstanceFunction<ID, IL, OD, OL>(){
//			@Override
//			public Annotation<OD, OL> apply(Annotation<ID, IL> input) {
//				return new AbstractTransformedAnnotatedInstance<ID, IL, OD, OL>(input){
//					@Override
//					protected OL transformedAIAnnotationValue(IL value) {
//						return annF.apply(value);
//					}
//					@Override
//					protected OD transformedAIInstanceValue(ID value) {
//						return dataF.apply(value);
//					}
//					@Override
//					protected String transformedAISourceValue(String value) {
//						return sourceF.apply(value);
//					}
//				};
//			};
//		};
//	}
//	
//	public static <ID, IL, OD, OL> Annotation<OD, OL> transformedAnnotatedInstance(
//			Annotation<ID, IL> annotation, Function<ID,OD> dataF, Function<IL,OL> labelF, Function<String,String> sourceF){
//		return Instances.<ID,IL,OD,OL>transformedAnnotatedInstanceFunction(dataF,labelF,sourceF).apply(annotation);
//	}
	


	//-------------------------------------------------------------
	// filtering instances
	//-------------------------------------------------------------
	public static <D,L> Predicate<FlatInstance<D,L>> labeledInstancePredicate(final Predicate<D> dataP, final Predicate<L> labelP, final Predicate<String> sourceP){
		return new Predicate<FlatInstance<D,L>>() {
			@Override
			public boolean apply(FlatInstance<D, L> input) {
				return dataP.apply(input.getData()) &&
						labelP.apply(input.getLabel()) &&
						sourceP.apply(input.getSource());
			}
		};
	}
	
	public static <D,L> boolean filterLabeledInstance(FlatInstance<D, L> label, Predicate<D> dataP, Predicate<L> labelP, Predicate<String> sourceP){
		return labeledInstancePredicate(dataP, labelP, sourceP).apply(label);
	}
	
	
	
//	//-------------------------------------------------------------
//	// filtering annotated instances
//	//-------------------------------------------------------------
//	public static <D,L> Predicate<Annotation<D,L>> annotatedInstancePredicate(final Predicate<D> dataP, final Predicate<L> labelP, final Predicate<String> sourceP){
//		return new Predicate<Annotation<D,L>>() {
//			@Override
//			public boolean apply(Annotation<D, L> input) {
//				return dataP.apply(input.getAnnotationInstance().getInstance().getValue()) &&
//						labelP.apply(input.getValue()) &&
//						sourceP.apply(input.getAnnotationInstance().getInstance().getSource());
//			}
//		};
//	}
//	
//	public static <D,L> boolean filterAnnotatedInstance(Annotation<D, L> label, Predicate<D> dataP, Predicate<L> labelP, Predicate<String> sourceP){
//		return annotatedInstancePredicate(dataP, labelP, sourceP).apply(label);
//	}
//
//	
//
//	
//	private static class AnnotationTransformer<IL, OL> implements Function<TimedAnnotation<IL>, TimedAnnotation<OL>> {
//		
//		private final Function<? super IL, ? extends OL> f;
//		
//		public AnnotationTransformer(Function<? super IL, ? extends OL> f) {
//			this.f = f;
//		}
//		
//		@Override
//		public TimedAnnotation<OL> apply(final TimedAnnotation<IL> from) {
//			return new TimedAnnotation<OL>() {
//
//				@Override
//				public OL getAnnotation() {
//					return f.apply(from.getAnnotation());
//				}
//
//				@Override
//				public TimedEvent getAnnotationTime() {
//					return from.getAnnotationTime();
//				}
//				
//				@Override
//				public TimedEvent getWaitTime() {
//				    return from.getWaitTime();
//				}
//			};
//		}
//	}
//	
	
	public static <D> Predicate<Instance<D>> sourcePredicate(
			final Predicate<String> predicate) {
		return new Predicate<Instance<D>>() {
			@Override
			public boolean apply(Instance<D> input) {
				return predicate.apply(input.getSource());
			}
		};
	}
	
	public static <D> boolean filterSource(Predicate<String> predicate, Instance<D> instance) {
		return Instances.<D>sourcePredicate(predicate).apply(instance);
	}
	
	public interface OneToManyLabeledInstanceFunction<ID, OD, L> extends Function<FlatInstance<ID,L>, Iterator<FlatInstance<OD,L>>> {
	}
	
	public static <ID, OD, L> OneToManyLabeledInstanceFunction<ID, OD, L> oneToManyDataFunction(
			final Function<ID, Iterator<OD>> dataF) {

		return new OneToManyLabeledInstanceFunction<ID, OD, L>() {

			@Override
			public Iterator<FlatInstance<OD, L>> apply(final FlatInstance<ID, L> label) {
				Iterator<OD> it = dataF.apply(label.getData());
				return Iterators.transform(it, new Function<OD, FlatInstance<OD,L>>(){
					@Override
					public FlatInstance<OD,L> apply(final OD data) {
						// return a bunch of labeled instances that are all based on 
						// the same instance but with different instance values
						return new AbstractTransformedFlatInstance<ID,L,OD,L>(label){
							@Override
							protected L transformedLabelValue(L value) {
								return value; // pass-through
							}
							@Override
							protected OD transformedInstanceValue(ID value) {
								return data; // new data
							}
							@Override
							protected String transformedInstanceSource(String value) {
								return value; // pass-through
							}
							@Override
							protected Long transformInstanceId(Long value) {
								return value; // pass-through
							}
							@Override
							protected Long transformAnnotatorId(Long value) {
								return value; // pass-through
							}
						};
					}
				});
			}

		};
	}

	// TODO(rah67) : move
	public static <T, F> Function<List<T>, List<F>> elementwiseSequenceFunction(final Function<T, F> function) {
		return new Function<List<T>, List<F>>() {
			@Override
			public List<F> apply(List<T> from) {
				return Lists.transform(from, function);
			}
		};
	}
	
//	public static <ID, OD> Iterable<Instance<OD>> transformData(Iterable<Instance<ID>> it, Function<ID, OD> f) {
//		return Pipes.<ID, OD>dataTransformingPipe(f).apply(it);
//	}
//
//	public static <D, L> Function<Instance<D>, D> dataExtractor() {
//		return new Function<Instance<D>, D>() {
//
//			@Override
//			public D apply(Instance<D> instance) {
//				return instance.getData();
//			}
//		};
//	}


	private static class LabelGetter implements Function<DatasetInstance, Integer> {
		@Override
		public Integer apply(DatasetInstance ta) {
			return ta.getLabel();
		}
	}
	
	public static <L> LabelGetter labelGetter() {
		return new LabelGetter();
	}
	
}
