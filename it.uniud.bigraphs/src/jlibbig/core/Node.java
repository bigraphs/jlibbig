package jlibbig.core;

import java.util.*;

/**
 * Describes a node of a bigraph.
 * 
 */
public interface Node extends Parent, Child, PlaceEntity {
	public List<? extends Port> getPorts();

	/**
	 * get the n-th port of a node
	 * 
	 * @param index
	 *            index of the port
	 * @return the index-th port
	 */
	public abstract Port getPort(int index);

	/**
	 * Get the node's control
	 * 
	 * @return the node's control
	 */
	public abstract Control getControl();

}
