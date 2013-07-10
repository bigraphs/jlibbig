package jlibbig.core;

/**
 * Describes innernames or control's ports of a bigraph.
 * @see Point
 */
interface EditablePoint extends Point, Owned {
	/**
	 * Set point's handle (outername or edge)
	 * @param handle point's new handle
	 */
	void setHandle(EditableHandle handle);
	@Override
	EditableHandle getHandle();
}
