package jlibbig;

public interface BigraphNode extends PlaceGraphNode, LinkGraphNode{
	/**  A bigraphical node is decorated with an immutable {@link BigraphControl}.
	 * @see jlibbig.GraphNode#getControl()
	 */
	@Override
	BigraphControl getControl();
}
