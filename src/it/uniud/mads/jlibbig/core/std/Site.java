package it.uniud.mads.jlibbig.core.std;

/**
 * Sites are leaves in the place graph structure and compose its inner interface.
 * Sites are identified within a bigraph by their index (position).
 * 
 * @see Root
 */
public interface Site extends Child, it.uniud.mads.jlibbig.core.Site {
	@Override
	public abstract EditableSite getEditable();
}
