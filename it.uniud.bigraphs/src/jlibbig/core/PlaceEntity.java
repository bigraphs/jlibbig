package jlibbig.core;

interface PlaceEntity {
	/**
	 * Check if a entity in the place graph is a parent.
	 * @return the result of the check
	 */
	boolean isParent();
	/**
	 * Check if a entity in the place graph is a child.
	 * @return the result of the check
	 */
	boolean isChild();
	/**
	 * Check if a entity in the place graph is a root.
	 * @return the result of the check
	 */
	boolean isRoot();
	/**
	 * Check if a entity in the place graph is a site.
	 * @return the result of the check
	 */
	boolean isSite();
	/**
	 * Check if a entity in the place graph is a node.
	 * @return the result of the check
	 */
	boolean isNode();
}