package jlibbig;

import java.util.*;

/**
 * Describes a node of a link graph. 
 * Nodes are identified by they immutable name and are characterized by an 
 * immutable list of ports consistent with their arity (@link LinkGraphControl).
 */
public interface LinkGraphNode extends GraphNode {	
	/** A link graph node presents an immutable list of ports consistently with the arity
	 * specified by its control (@link LinkGraphControl)
	 * @return the list of ports
	 */
	public List<Port> getPorts();
	
	/** Returns the port identified by the specified index; otherwise {@literal null}.
	 * @param index the port to be returned
	 * @return the port at the given index
	 */
	public Port getPort(int index);
	
	/** A link graph node is decorated with an immutable {@link LinkGraphControl}.
	 * @see jlibbig.GraphNode#getControl()
	 */
	@Override
	LinkGraphControl getControl();
}
