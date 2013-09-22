package it.uniud.mads.jlibbig.core.std;

/**
 * Describes a name of an internal link graph interface. Differently from outer
 * names, inners can be linked but not link (i.e. they appear in the domain of a
 * link map but not in its codomain).
 */
public interface InnerName extends Point, LinkFacet,
		it.uniud.mads.jlibbig.core.InnerName {
	@Override
	public abstract EditableInnerName getEditable();
}
