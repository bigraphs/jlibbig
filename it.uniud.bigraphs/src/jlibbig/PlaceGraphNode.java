package jlibbig;

/**
 * Describes a node of a place graph. 
 * Nodes are identified by they immutable name.
 * They are can appear both as children and as parents in the parent map of a place graph.
 */
public interface PlaceGraphNode extends GraphNode, PlaceGraph.Child , PlaceGraph.Parent {
	/** A place graph node is decorated with an immutable {@link PlaceGraphControl}.
	 * @see jlibbig.GraphNode#getControl()
	 */
	@Override
	PlaceGraphControl getControl();
}
