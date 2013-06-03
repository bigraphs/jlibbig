package jlibbig.core;

abstract class EditableLinkFacet implements LinkFacet, EditableNamed, Replicable{
	protected String name;
	
	protected EditableLinkFacet() {
		this("X_" + AbstNamed.generateName());
	}

	protected EditableLinkFacet(String name) {
		setName(name);
	}
	
	@Override
	public void setName(String name) {
		if (name == null || name.isEmpty())
			throw new IllegalArgumentException("Name can not be empty.");
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		EditableLinkFacet other;
		try {
			other = (EditableLinkFacet) obj;
		} catch (ClassCastException e) {
			return false;
		}
		return name.equals(other.name);
	}
}
