package it.uniud.mads.jlibbig.core;

import it.uniud.mads.jlibbig.core.Owned;
/**
 * Describes entities composing the link graph such as handles ({@link Handle})
 * and points ({@link Point}).
 * 
 * @see PlaceEntity
 */
public interface LinkEntity extends Owned {
	/**
	 * Checks if a entity is a handle.
	 * 
	 * @return a boolean indicating whether this entity is an handle.
	 */
	boolean isHandle();

	/**
	 * Checks if this entity is a point.
	 * 
	 * @return a boolean indicating whether this entity is a point.
	 */
	boolean isPoint();

	/**
	 * Checks if this entity is a port.
	 * 
	 * @return a boolean indicating whether this entity is a port.
	 */
	boolean isPort();

	/**
	 * Checks if this entity is an inner name.
	 * 
	 * @return a boolean indicating whether this entity is an inner name.
	 */
	boolean isInnerName();

	/**
	 * Checks if this entity is an outer name.
	 * 
	 * @return a boolean indicating whether this entity is a outer name.
	 */
	boolean isOuterName();

	/**
	 * Checks if this entity is an edge.
	 * 
	 * @return a boolean indicating whether this entity is a edge.
	 */
	boolean isEdge();

}