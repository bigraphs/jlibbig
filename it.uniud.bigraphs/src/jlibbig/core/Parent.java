package jlibbig.core;

import java.util.Set;

public interface Parent extends PlaceEntity{
	/**
	 * Get the set of children
	 * @return the set of children of this parent
	 */
	Set<? extends Child> getChildren();
}
