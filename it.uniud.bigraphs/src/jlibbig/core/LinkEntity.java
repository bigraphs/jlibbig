package jlibbig.core;

import jlibbig.core.abstractions.Owned;

public interface LinkEntity extends Owned, jlibbig.core.abstractions.LinkEntity {
	/**
	 * Checks if a entity is a handle.
	 * 
	 * @return the result of the check
	 */
	boolean isHandle();

	/**
	 * Checks if a entity is a point.
	 * 
	 * @return the result of the check
	 */
	boolean isPoint();

	/**
	 * Checks if a entity is a port.
	 * 
	 * @return the result of the check
	 */
	boolean isPort();

	/**
	 * Check if a entity is an inner name.
	 * 
	 * @return the result of the check
	 */
	boolean isInnerName();

	/**
	 * Check if a entity is an outer name.
	 * 
	 * @return the result of the check
	 */
	boolean isOuterName();

	/**
	 * Check if a entity is an edge.
	 * 
	 * @return the result of the check
	 */
	boolean isEdge();

}
