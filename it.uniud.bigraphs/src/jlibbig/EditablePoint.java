package jlibbig;

interface EditablePoint extends Point {
	void setHandle(EditableHandle handle);
	@Override
	EditableHandle getHandle();
}
