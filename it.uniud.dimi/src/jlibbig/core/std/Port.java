package jlibbig.core.std;

/**
 * Control's ports
 * 
 * @see Control
 */
public interface Port extends Point, jlibbig.core.Port {

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