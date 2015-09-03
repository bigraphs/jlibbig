package it.uniud.mads.jlibbig.core.std;

/**
 * Edges are handles not accessible through the outer interface on the contrary
 * of outer names ({@link OuterName}).
 */
public interface Edge extends Handle, it.uniud.mads.jlibbig.core.Edge {
	@Override
	public abstract EditableEdge getEditable();
}
