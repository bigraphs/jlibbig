package jlibbig.core;

import jlibbig.core.AbstractNamed;
import jlibbig.core.Named;

/**
 * Describes a control. Each node of a bigraph have its control.
 */
public class Control extends AbstractNamed implements Named {

	protected int arity;

	public Control(int arity) {
		this("C_" + AbstractNamed.generateName(), arity);
	}

	public Control(String name, int arity) {
		super(name);
		this.arity = arity;
	}

	/**
	 * Get control's arity. This correspond to the number of ports of a node.
	 * 
	 * @return control's arity.
	 */
	public int getArity() {
		return arity;
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
		return getName() + ":" + arity;
	}
}
