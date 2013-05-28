package jlibbig.core;

interface EditablePoint extends Point, Owned {
	void setHandle(EditableHandle handle);
	@Override
	EditableHandle getHandle();
}
