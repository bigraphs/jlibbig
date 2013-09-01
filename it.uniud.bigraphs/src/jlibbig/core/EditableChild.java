package jlibbig.core;

interface EditableChild extends Child, ReplicableEx, Owned {
	void setParent(EditableParent parent);
	@Override
	public abstract EditableParent getParent();
	
	@Override
	public abstract EditableChild replicate();
}
