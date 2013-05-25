package jlibbig.core;

interface EditablePoint extends Point {
	void setHandle(EditableHandle handle);
	@Override
	EditableHandle getHandle();
}
