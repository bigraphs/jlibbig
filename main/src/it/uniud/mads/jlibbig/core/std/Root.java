package it.uniud.mads.jlibbig.core.std;

/**
 * Describes a root of a place graph.
 */
public interface Root extends Parent, PlaceEntity,
		it.uniud.mads.jlibbig.core.Root {
	@Override
	public abstract EditableRoot getEditable();
}
