package jlibbig.core.abstractions;

public interface PlaceEntity extends Owned {

	/**
	 * Check if a entity in the place graph is a parent.
	 * 
	 * @return the result of the check
	 */
	public abstract boolean isParent();

	/**
	 * Check if a entity in the place graph is a child.
	 * 
	 * @return the result of the check
	 */
	public abstract boolean isChild();

	/**
	 * Check if a entity in the place graph is a root.
	 * 
	 * @return the result of the check
	 */
	public abstract boolean isRoot();

	/**
	 * Check if a entity in the place graph is a site.
	 * 
	 * @return the result of the check
	 */
	public abstract boolean isSite();

	/**
	 * Check if a entity in the place graph is a node.
	 * 
	 * @return the result of the check
	 */
	public abstract boolean isNode();

}