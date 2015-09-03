package it.uniud.mads.jlibbig.core;

/**
 * Points are link entities connected by the hyper-edges composing the link
 * graphs. Points are inner names or ports depending on whereas they belong to
 * an inner interface or to a node.
 */
public interface Point extends LinkEntity {
	/**
	 * Get the current handler (outername or edge) of a point.
	 * 
	 * @return the current handler
	 */
	public abstract Handle getHandle();
}
