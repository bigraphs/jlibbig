package jlibbig;

/**
 * Describes a name of an internal link graph interface.
 * Differently from outer names, inners can be linked but not link (i.e. they 
 * appear in the domain of a link map but not in its codomain).
 */
public final class InnerName extends LinkGraphFacet implements LinkGraphAbst.Linked {

	/** Creates an inner name with a generated name.
	 * The name is in the reserved form <code>"F_%d"</code>.
	 * @see LinkGraphFacet#LinkGraphFacet()
	 */
	public InnerName() {}
	
	/** Creates an inner name with the specified name.
	 * @param name the name to be used
	 */
	public InnerName(String name) {
		super(name);
	}
}
