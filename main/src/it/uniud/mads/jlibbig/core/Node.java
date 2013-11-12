package it.uniud.mads.jlibbig.core;

import java.util.*;

/**
 * Describes nodes of bigraphs for the given kind of controls. Nodes are labelled by
 * some control which defines the semantic role of a node and properties it has to satisfy
 * such as its arity that is the number of ports exposed by a node. Ports are
 * end-points for hyper-edges composing the link graph.
 * 
 * @param <C> the kind of control assigned to the node.
 */
public interface Node<C extends Control> extends Parent, Child, PlaceEntity {
	public List<? extends Port<? extends C>> getPorts();

	/**
	 * Gets the n-th port of this node.
	 * 
	 * @param index
	 *            index of the port
	 * @return the index-th port
	 */
	public abstract Port<C> getPort(int index);

	/**
	 * Get the control assigned to this node.
	 * 
	 * @return the node's control
	 */
	public abstract C getControl();

}
