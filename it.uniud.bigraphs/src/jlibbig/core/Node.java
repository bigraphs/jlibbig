package jlibbig.core;

import java.util.*;

import jlibbig.core.attachedProperties.PropertyTarget;

/**
 * Describes a node of a bigraph.
 * 
 */
public interface Node extends Parent, Child, jlibbig.core.abstractions.Node,
		PropertyTarget {
	public List<? extends Port> getPorts();

	/**
	 * get the n-th port of a node
	 * 
	 * @param index
	 *            index of the port
	 * @return the index-th port
	 */
	@Override
	public abstract Port getPort(int index);

	/**
	 * Get the node's control
	 * 
	 * @return the node's control
	 */
	@Override
	public abstract Control getControl();

	@Override
	public abstract EditableNode getEditable();
}
