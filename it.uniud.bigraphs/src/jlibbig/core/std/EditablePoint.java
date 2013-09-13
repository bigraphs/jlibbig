package jlibbig.core.std;

/**
 * Describes innernames or control's ports of a bigraph.
 * 
 * @see Point
 */
interface EditablePoint extends Point {
	/**
	 * Set point's handle (outername or edge)
	 * 
	 * @param handle
	 *            point's new handle
	 */
	public abstract void setHandle(EditableHandle handle);

	@Override
	public abstract EditableHandle getHandle();
}