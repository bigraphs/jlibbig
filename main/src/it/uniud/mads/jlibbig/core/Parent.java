package it.uniud.mads.jlibbig.core;

import java.util.Collection;

public interface Parent extends PlaceEntity {
	/**
	 * Get the set of children
	 * 
	 * @return the set of children of this parent
	 */
	public abstract Collection<? extends Child> getChildren();
}
