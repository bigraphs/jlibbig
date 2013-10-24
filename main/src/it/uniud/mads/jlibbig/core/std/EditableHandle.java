package it.uniud.mads.jlibbig.core.std;

import java.util.*;

/**
 * Handle: Outername or Edge
 * 
 */
interface EditableHandle extends Handle, Replicable, EditableOwned {
	/**
	 * Get the set of control's ports and innernames of an handle
	 * 
	 * @return set of points
	 */
	public abstract Collection<EditablePoint> getEditablePoints();

	/**
	 * Add an innername or port to this handle.
	 * 
	 * @param point
	 *            innername or port that will be added
	 */
	public abstract void linkPoint(EditablePoint point);

	/**
	 * remove a point from this handle
	 * 
	 * @param point
	 *            innername or port that will be removed
	 */
	public abstract void unlinkPoint(EditablePoint point);

	@Override
	public abstract EditableHandle replicate();
}
