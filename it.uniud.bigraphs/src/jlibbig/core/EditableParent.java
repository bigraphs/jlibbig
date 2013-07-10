package jlibbig.core;

import java.util.*;

/**
 * Describes roots or nodes of a place graph.
 * @see Parent
 *
 */
interface EditableParent extends Parent, Replicable, Owned {
	/**
	 * Get the set of children. This set and every object inside it can be modified.
	 * @return the set of parent's children
	 */
	Set<EditableChild> getEditableChildren();
	/**
	 * Add a child to this parent.
	 * @param child the child that will be added
	 */
	void addChild(EditableChild child);
	/**
	 * Remove a child
	 * @param child the child that will be removed
	 */
	void removeChild(EditableChild child);
	
	/**
	 * @see Replicable#replicate()
	 */
	EditableParent replicate();
}
