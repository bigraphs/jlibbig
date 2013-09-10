package jlibbig.core.abstractions;

/**
 * Control's ports
 * @see Control
 */
public interface Port extends Point{

	/**
	 * Get the port's node
	 * @return the port's node
	 */
	public abstract Node getNode();

	/**
	 * Get the number of this port in the node.
	 * @return the number of this port
	 */
	public abstract int getNumber();
}