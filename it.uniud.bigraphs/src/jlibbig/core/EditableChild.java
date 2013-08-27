package jlibbig.core;

interface EditableChild extends Child, ReplicableEx, Owned {
	void setParent(EditableParent parent);
	@Override
	EditableChild replicate();
	@Override
	EditableParent getParent();
}
