package edu.byu.nlp.data;

import com.google.common.base.Preconditions;

import edu.byu.nlp.annotationinterface.AutomaticAnnotation;

public class FlatLabeledInstance<D,L> extends AbstractFlatInstance<D, L> {

	public static long NULL_ANNOTATOR = -1;
	
	private AutomaticAnnotation<D, L> labeledInstance;

	public FlatLabeledInstance(AutomaticAnnotation<D, L> labeledInstance){
		Preconditions.checkNotNull(labeledInstance,"label must not be null");
		Preconditions.checkNotNull(labeledInstance.getInstance(),"instance must not be null");
		Preconditions.checkNotNull(labeledInstance.getModel(),"model must not be null");
		Preconditions.checkNotNull(labeledInstance,"must not be null");
		this.labeledInstance=labeledInstance;
	}
	
	@Override
	public D getData() {
		return labeledInstance.getInstance().getValue();
	}

	@Override
	public L getLabel() {
		return labeledInstance.getValue();
	}

	@Override
	public boolean isAnnotation() {
		return false;
	}

	@Override
	public String getSource() {
		return labeledInstance.getInstance().getSource();
	}

	@Override
	public long getStartTimestamp() {
		return FlatAnnotatedInstance.NULL_TIMESTAMP;
	}

	@Override
	public long getEndTimestamp() {
		return FlatAnnotatedInstance.NULL_TIMESTAMP;
	}

	@Override
	public long getAnnotator() {
		return labeledInstance.getModel().getId();
	}

	@Override
	public long getInstanceId() {
		return labeledInstance.getInstance().getId();
	}

	@Override
	public L getAutomaticLabel() {
		return getLabel();
	}

	@Override
	public long getAutomaticLabelerId() {
		return labeledInstance.getModel().getId();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (obj==null || !(obj instanceof FlatInstance)){
			return false;
		}
		return ((FlatInstance<D,L>)obj).getInstanceId() == getInstanceId();
	}
	
	@Override
	public int hashCode() {
		return (int) getInstanceId();
	}
}
