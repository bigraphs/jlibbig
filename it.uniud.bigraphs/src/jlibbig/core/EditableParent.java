package jlibbig.core;

import java.util.*;

import jlibbig.core.abstractions.Owned;
import jlibbig.core.attachedProperties.PropertyTarget;

/**
 * Describes roots or nodes of a place graph.
 * 
 * @see Parent
 * 
 */
interface EditableParent extends Parent, ReplicableEx, Owned, PropertyTarget {
	/**
	 * Get the set of children. This set and every object inside it can be
	 * modified.
	 * 
	 * @return the set of parent's children
	 */
	public abstract Collection<EditableChild> getEditableChildren();

	/**
	 * Add a child to this parent.
	 * 
	 * @param child
	 *            the child that will be added
	 */
	public abstract void addChild(EditableChild child);

	/**
	 * Remove a child
	 * 
	 * @param child
	 *            the child that will be removed
	 */
	public abstract void removeChild(EditableChild child);

	public abstract EditableRoot getRoot();

	/**
	 * @see ReplicableEx#replicate()
	 */
	@Override
	public abstract EditableParent replicate();
}
