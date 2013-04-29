package jlibbig;

/**
 * Describes a name of an internal link graph interface.
 * Differently from inner names, outer ones can link but not be linked (i.e. they
 * appear in the codomain of a link map nut not in its domain).
 */
public final class OuterName extends LinkGraphFacet implements LinkGraphAbst.Linker {

	/** Creates an outer name with a generated name.
	 * The name is in the reserved form <code>"F_%d"</code>.
	 * @see LinkGraphFacet#LinkGraphFacet()
	 */
	public OuterName() {}
	
	/** Creates an outer name with the specified name.
	 * @param name the name to be used
	 */
	public OuterName(String name) {
		super(name);
	}
}
