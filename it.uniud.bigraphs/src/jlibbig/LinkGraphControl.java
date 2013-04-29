package jlibbig;

/**
 * Describes a control assignable to nodes of a link graph.
 * Link graph controls are uniquely identified by their immutable name and
 * present an immutable finite ordinal representing the arity (number of ports)
 * of the nodes they decorate.
 */
public interface LinkGraphControl extends GraphControl{
	/** Returns an integer representing the arity of the control.
	 * @return the arity
	 */
	int getArity();
}
