package jlibbig;

public abstract class LinkGraphFacet extends Named{
	public LinkGraphFacet() {}
	
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
