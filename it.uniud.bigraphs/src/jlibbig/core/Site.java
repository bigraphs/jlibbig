package jlibbig.core;

/**
 * Describes a site of a place graph.
 *
 */
public interface Site  extends Child, jlibbig.core.abstractions.Site {
	@Override
	public abstract EditableSite getEditable();
}
