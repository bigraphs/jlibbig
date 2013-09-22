package it.uniud.mads.jlibbig.core.std;

/**
 * In a bigraph, a point can either be a node's port or a innername
 * 
 */
public interface Point extends LinkEntity, it.uniud.mads.jlibbig.core.Point {
	/**
	 * Get the current handler (outername or edge) of a point.
	 * 
	 * @return the current handler
	 */
	@Override
	public abstract EditableHandle getHandle();

	public abstract EditablePoint getEditable();
}
