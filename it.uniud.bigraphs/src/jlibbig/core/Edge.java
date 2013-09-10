package jlibbig.core;

/**
 * Describes an edge of a link graph.
 * 
 */
public interface Edge extends Handle, jlibbig.core.abstractions.Edge {
	@Override
	public abstract EditableEdge getEditable();
}
