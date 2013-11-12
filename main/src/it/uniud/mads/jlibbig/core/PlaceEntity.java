package it.uniud.mads.jlibbig.core;

/**
 * Describes entities composing the place graph such as roots ({@link Root}),
 * nodes ({@link Node}), and sites ({@link Site}).
 * 
 * @see LinkEntity
 */
public interface PlaceEntity extends Owned {

	/**
	 * Checks if this entity in the place graph is a parent.
	 * 
	 * @return a boolean indicating whether this entity is a parent. 
	 */
	public abstract boolean isParent();

	/**
	 * Checks if this entity in the place graph is a child.
	 * 
	 * @return a boolean indicating whether this entity is a child.
	 */
	public abstract boolean isChild();

	/**
	 * Checks if this entity in the place graph is a root.
	 * 
	 * @return a boolean indicating whether this entity is a root.
	 */
	public abstract boolean isRoot();

	/**
	 * Checks if this entity in the place graph is a site.
	 * 
	 * @return a boolean indicating whether this entity is a site.
	 */
	public abstract boolean isSite();

	/**
	 * Checks if this entity in the place graph is a node.
	 * 
	 * @return a boolean indicating whether this entity is a node.
	 */
	public abstract boolean isNode();

}