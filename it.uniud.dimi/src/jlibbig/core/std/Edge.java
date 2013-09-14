package jlibbig.core.std;

/**
 * Describes an edge of a link graph.
 * 
 */
public interface Edge extends Handle, jlibbig.core.Edge {
	@Override
	public abstract EditableEdge getEditable();
}
