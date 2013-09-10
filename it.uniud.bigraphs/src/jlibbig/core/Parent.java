package jlibbig.core;

import java.util.Collection;

public interface Parent extends PlaceEntity, jlibbig.core.abstractions.Parent {
	/**
	 * Get the set of children
	 * 
	 * @return the set of children of this parent
	 */
	@Override
	public abstract Collection<? extends Child> getChildren();

	public abstract EditableParent getEditable();
}
