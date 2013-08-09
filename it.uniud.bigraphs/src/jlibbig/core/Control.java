package jlibbig.core;

/**
 * Describes a control. Each node of a bigraph have its control.
 */
public class Control extends AbstractNamed implements Named {

	private final boolean active;
	private final int arity;

	public Control(boolean active, int arity) {
		this("C_" + AbstractNamed.generateName(), active, arity);
	}

	public Control(String name, boolean active, int arity) {
		super(name);
		this.arity = arity;
		this.active = active;
	}

	/**
	 * Get control's arity. This correspond to the number of ports of a node.
	 * 
	 * @return control's arity.
	 */
	public final int getArity() {
		return arity;
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
		result = prime * result + arity;
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
		if (arity != other.arity || super.getName() != other.getName())
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getName() + ":(" + arity + ((active) ? ",a)" : ",p)");
	}
}
