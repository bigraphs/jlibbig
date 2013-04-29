package jlibbig;

/**
 * Describes a name (facet) composing a linkg graph interface {@link LinkGraphFace}.
 */
public abstract class LinkGraphFacet extends Named{
	

	/** Creates a facet with a generated name.
	 * The name is in the reserved form <code>"F_%d"</code> ({@link Named#generateName()}).
	 */
	public LinkGraphFacet() {
		super("F_" + generateName());
	}
	
	/** Creates a facet with the specified name.
	 * The name pattern <code>"F_%d"</code> is reserved ({@link Named#generateName()}).
	 * @param name the name to be used
	 */
	public LinkGraphFacet(String name) {
		super(name);
	}
	
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		LinkGraphFacet other = null;
		try {
			other = (LinkGraphFacet) obj;
		} catch (ClassCastException e) {
			return false;
		}
		return super.getName().equals(other.getName());
	}
}
