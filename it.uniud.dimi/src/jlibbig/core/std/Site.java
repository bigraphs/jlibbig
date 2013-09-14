package jlibbig.core.std;

/**
 * Describes a site of a place graph.
 * 
 */
public interface Site extends Child, jlibbig.core.Site {
	@Override
	public abstract EditableSite getEditable();
}
