package jlibbig.core.std;

/**
 * Describes a child (node or site) of a bigraph
 * 
 */
public interface Child extends PlaceEntity, jlibbig.core.Child {
	/**
	 * Get the parent (node or root) of a child.
	 * 
	 * @return the child's parent.
	 */
	@Override
	public abstract Parent getParent();

	public abstract EditableChild getEditable();
}
