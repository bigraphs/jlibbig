package jlibbig.core.std;

/**
 * Describes a name of an internal link graph interface. Differently from inner
 * names, outer ones can link but not be linked (i.e. they appear in the
 * codomain of a link map nut not in its domain).
 */
public interface OuterName extends Handle, LinkFacet,
		jlibbig.core.OuterName {
	@Override
	public abstract EditableOuterName getEditable();
}
