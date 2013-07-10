package jlibbig.core;

/**
 * Describes a control. Each node of a bigraph have its control.
 */
public interface Control extends Named{
	/**
	 * Get control's arity. This correspond to the number of ports of a node.
	 * @return control's arity.
	 */
	int getArity();
	
	/**
	 * Check if a control is active.
	 * @return the result of the check.
	 */
	boolean isActive();
}
