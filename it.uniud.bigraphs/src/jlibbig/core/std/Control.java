package jlibbig.core.std;

import jlibbig.core.AbstractNamed;

/**
 * Describes a control. Each node of a bigraph have its control.
 */

public final class Control extends jlibbig.core.Control {

	private final boolean active;

	public Control(boolean active, int arity) {
		this("C_" + AbstractNamed.generateName(), active, arity);
	}

	public Control(String name, boolean active, int arity) {
		super(name, arity);
		this.active = active;
	}

	/**
	 * Check if a control is active.
	 * 
	 * @return the result of the check.
	 */
	public final boolean isActive() {
		return active;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + getArity();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Control other = (Control) obj;
		if (getArity() != other.getArity()
				|| super.getName() != other.getName())
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getName() + ":(" + getArity() + ((active) ? ",a)" : ",p)");
	}

}