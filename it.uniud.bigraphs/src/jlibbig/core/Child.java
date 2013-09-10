package jlibbig.core;

/**
 * Describes a child (node or site) of a bigraph
 * 
 */
public interface Child extends PlaceEntity, jlibbig.core.abstractions.Child {
	/**
	 * Get the parent (node or root) of a child.
	 * 
	 * @return the child's parent.
	 */
	@Override
	public abstract Parent getParent();

	public abstract EditableChild getEditable();
}
