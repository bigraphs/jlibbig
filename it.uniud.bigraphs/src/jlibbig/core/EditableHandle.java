package jlibbig.core;

import java.util.*;

/**
 * Handle: Outername or Edge
 *
 */
interface EditableHandle extends Handle, ReplicableEx, Owned, EditableOwned{
	/**
	 * Get the set of control's ports and innernames of an handle
	 * @return set of points
	 */
	public abstract Set<EditablePoint> getEditablePoints();
	/**
	 * Add an innername or port to this handle.
	 * @param point innername or port that will be added
	 */
	public abstract void linkPoint(EditablePoint point);
	/**
	 * remove a point from this handle
	 * @param point innername or port that will be removed
	 */
	public abstract void unlinkPoint(EditablePoint point);
	
	@Override
	public abstract EditableHandle replicate();
}
