package jlibbig.core;

/**
 * Control's ports
 * 
 * @see Control
 */
public interface Port extends Point, jlibbig.core.abstractions.Port {

	/**
	 * Get the port's node
	 * 
	 * @return the port's node
	 */
	@Override
	public abstract Node getNode();

	@Override
	public abstract EditableNode.EditablePort getEditable();
}