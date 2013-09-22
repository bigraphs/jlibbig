package it.uniud.mads.jlibbig.core.std;

/**
 * Describes an edge of a link graph.
 * 
 */
public interface Edge extends Handle, it.uniud.mads.jlibbig.core.Edge {
	@Override
	public abstract EditableEdge getEditable();
}
