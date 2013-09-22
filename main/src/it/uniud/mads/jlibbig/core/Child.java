package it.uniud.mads.jlibbig.core;

/**
 * Describes a child (node or site) of a bigraph
 * 
 */
public interface Child extends PlaceEntity {
	/**
	 * Get the parent (node or root) of a child.
	 * 
	 * @return the child's parent.
	 */
	public abstract Parent getParent();
}
