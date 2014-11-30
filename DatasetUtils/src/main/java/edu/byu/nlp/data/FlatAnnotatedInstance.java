package edu.byu.nlp.data;

import com.google.common.base.Preconditions;

import edu.byu.nlp.annotationinterface.java.Annotation;
import edu.byu.nlp.middleware.TimelineEvent;

public class FlatAnnotatedInstance<D,L> extends AbstractFlatInstance<D, L> {
	
	private Annotation<D, L> annotatedInstance;
	private final long startTs;
	private final long endTs;
	
	public FlatAnnotatedInstance(Annotation<D, L> annotation){
		Preconditions.checkNotNull(annotation,"annotation must not be null");
		Preconditions.checkNotNull(annotation.getAnnotationInstance(),"annotation instance must not be null");
		Preconditions.checkNotNull(annotation.getAnnotationInstance().getInstance(),"instance must not be null");
		Preconditions.checkNotNull(annotation.getAnnotationInstance().getAnnotator(),"annotator must not be null");
		
		this.annotatedInstance=annotation;
		if (annotation.getAnnotationInstance().getTimelineEvents()!=null && 
				annotation.getAnnotationInstance().getTimelineEvents().size()>=2 &&
				annotation.getAnnotationInstance().getTimelineEvents().get(0).getEventName().equals(TimelineEvent.ANNOTATOR_GAIN_FOCUS) &&
				annotation.getAnnotationInstance().getTimelineEvents().get(1).getEventName().equals(TimelineEvent.ANNOTATOR_LOSE_FOCUS)
			){
			this.startTs = annotation.getAnnotationInstance().getTimelineEvents().get(0).getTimestamp().getTime();
			this.endTs = annotation.getAnnotationInstance().getTimelineEvents().get(1).getTimestamp().getTime();
		}
		else{
			this.startTs = NULL_TIMESTAMP;
			this.endTs = NULL_TIMESTAMP;
		}
	}
	
	@Override
	public D getData() {
		return null;
		// return null for efficiency and ease of processing. The data will be attached 
		// only to labeledinstance (of which there will be one per annotation, even if 
		// the label is unknown.)
	}

	@Override
	public L getLabel() {
		return annotatedInstance.getValue();
	}

	@Override
	public boolean isAnnotation() {
		return true;
	}

	@Override
	public String getSource() {
		return annotatedInstance.getAnnotationInstance().getInstance().getSource();
	}

	@Override
	public long getStartTimestamp() {
		return startTs;
	}

	@Override
	public long getEndTimestamp() {
		return endTs;
	}

	@Override
	public long getAnnotator() {
		return annotatedInstance.getAnnotationInstance().getAnnotator().getId();
	}

	@Override
	public long getInstanceId() {
		return annotatedInstance.getAnnotationInstance().getInstance().getId();
	}

	@Override
	public L getAutomaticLabel() {
		if (annotatedInstance.getAnnotationInstance()==null){
			return null;
		}
		return annotatedInstance.getAnnotationInstance().getAutomaticAnnotation().getValue();
	}

	@Override
	public long getAutomaticLabelerId() {
		if (annotatedInstance.getAnnotationInstance()==null){
			return NULL_ID;
		}
		return annotatedInstance.getAnnotationInstance().getAutomaticAnnotation().getModel().getId();
	}

	
}
