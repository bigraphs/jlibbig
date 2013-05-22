package jlibbig;

interface EditableChild extends Child, Replicable {
	void setParent(EditableParent parent);
	@Override
	EditableChild replicate();
	@Override
	EditableParent getParent();
}
