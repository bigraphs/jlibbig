package jlibbig.core;

/**
 * In a bigraph, a point can either be a node's port or a innername
 *
 */
public interface Point extends LinkEntity, jlibbig.core.abstractions.Point{
	/**
	 * Get the current handler (outername or edge) of a point.
	 * @return the current handler
	 */
	@Override
	public abstract Handle getHandle();
	
	public abstract EditablePoint getEditable();
}
