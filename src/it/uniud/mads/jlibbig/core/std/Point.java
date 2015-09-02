package it.uniud.mads.jlibbig.core.std;


/**
 * Points are link entities connected by the hyper-edges composing the link
 * graphs. Points are inner names or ports depending on whereas they belong to
 * an inner interface or to a node.
 */
public interface Point extends LinkEntity, it.uniud.mads.jlibbig.core.Point {
	@Override
	public abstract EditableHandle getHandle();

	public abstract EditablePoint getEditable();
}
