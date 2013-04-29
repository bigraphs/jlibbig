package jlibbig;

/**
 * Describes a node of a composable graph structure (e.g. place graphs link graphs and bigraphs).
 * The pattern <code>"N_%d"</code> is reserved for automatically generated names ({@link Named#generateName()}). 
 */
public interface GraphNode {
	/** A node is identified uniquely by its name
	 * @return the name of the node
	 */
	String getName();
	
	/** A node is decorated by a control
	 * @return the control assigned to the node
	 */
	GraphControl getControl();
}
