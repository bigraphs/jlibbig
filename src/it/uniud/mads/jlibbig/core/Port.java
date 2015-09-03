package it.uniud.mads.jlibbig.core;

/**
 * Ports are end-points for hyper-edges composing the link graphs and are
 * exposed by nodes structured in the place graph composing the bigraph together
 * with the aforementioned link graph. Despite a link graph {@link Point}, ports
 * belong to a node and are identified by their number or position w.r.t. that
 * node.
 * 
 * @param <C>
 *            the kind of control assigned nodes exposing these ports.
 */
public interface Port<C extends Control> extends Point {

	/**
	 * Gets the node exposing this port.
	 * 
	 * @return the port's node
	 */
	public abstract Node<C> getNode();

	/**
	 * Gets the number identifying this port w.r.t. its node.
	 * 
	 * @return the number of this port
	 */
	public abstract int getNumber();
}