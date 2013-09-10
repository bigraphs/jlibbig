package jlibbig.core;

import jlibbig.core.abstractions.AbstractNamed;

abstract class EditableLinkFacet implements LinkFacet, EditableNamed, ReplicableEx{
	
	protected String name;
	protected final ReplicateListenerContainer rep = new ReplicateListenerContainer();
	
	protected EditableLinkFacet() {
		this("X_" + AbstractNamed.generateName());
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
	public void registerListener(ReplicateListener listener) {
		rep.registerListener(listener);
	}

	@Override
	public boolean unregisterListener(ReplicateListener listener) {
		return rep.unregisterListener(listener);
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 83;
		return prime * name.hashCode();
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
	
//	@Override
//	public int compareTo(String arg0) {
//		return this.name.compareTo(arg0);
//	}
}
