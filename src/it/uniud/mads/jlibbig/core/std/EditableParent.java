package it.uniud.mads.jlibbig.core.std;

import java.util.*;

import it.uniud.mads.jlibbig.core.Owned;
import it.uniud.mads.jlibbig.core.attachedProperties.PropertyTarget;
import it.uniud.mads.jlibbig.core.attachedProperties.Replicable;

interface EditableParent extends Parent, Replicable, Owned, PropertyTarget {
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
	 * @see Replicable#replicate()
	 */
	@Override
	public abstract EditableParent replicate();
}
