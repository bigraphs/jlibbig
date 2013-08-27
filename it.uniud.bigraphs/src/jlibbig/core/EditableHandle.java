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
	Set<EditablePoint> getEditablePoints();
	/**
	 * Add an innername or port to this handle.
	 * @param point innername or port that will be added
	 */
	void linkPoint(EditablePoint point);
	/**
	 * remove a point from this handle
	 * @param point innername or port that will be removed
	 */
	void unlinkPoint(EditablePoint point);
	@Override
	EditableHandle replicate();
}
