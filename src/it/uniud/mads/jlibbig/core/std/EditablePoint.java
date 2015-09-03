package it.uniud.mads.jlibbig.core.std;


interface EditablePoint extends Point {
	/**
	 * Set point's handle.
	 * 
	 * @param handle
	 *            point's new handle
	 */
	public abstract void setHandle(EditableHandle handle);

	@Override
	public abstract EditableHandle getHandle();
}