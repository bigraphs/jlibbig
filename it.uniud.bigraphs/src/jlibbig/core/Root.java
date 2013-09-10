package jlibbig.core;

/**
 * Describes a root of a place graph.
 */
public interface Root extends Parent, PlaceEntity,
		jlibbig.core.abstractions.Root {
	@Override
	public abstract EditableRoot getEditable();
}
