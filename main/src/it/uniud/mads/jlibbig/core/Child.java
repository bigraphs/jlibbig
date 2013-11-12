package it.uniud.mads.jlibbig.core;


/**
 * Place graph entities are organised in tree-like structures.
 * 
 * @see Parent
 */
public interface Child extends PlaceEntity {
	/**
	 * Gets the parent of this child.
	 * 
	 * @return the child's parent.
	 */
	public abstract Parent getParent();
}
