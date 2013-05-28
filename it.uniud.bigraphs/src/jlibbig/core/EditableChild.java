package jlibbig.core;

interface EditableChild extends Child, Replicable, Owned {
	void setParent(EditableParent parent);
	@Override
	EditableChild replicate();
	@Override
	EditableParent getParent();
}
