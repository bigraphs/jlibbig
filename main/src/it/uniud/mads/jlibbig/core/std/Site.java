package it.uniud.mads.jlibbig.core.std;

/**
 * Describes a site of a place graph.
 * 
 */
public interface Site extends Child, it.uniud.mads.jlibbig.core.Site {
	@Override
	public abstract EditableSite getEditable();
}
