package jlibbig.core.std;

/**
 * Describes a root of a place graph.
 */
public interface Root extends Parent, PlaceEntity,
		jlibbig.core.Root {
	@Override
	public abstract EditableRoot getEditable();
}
