package it.uniud.mads.jlibbig.core;

import it.uniud.mads.jlibbig.core.util.NameGenerator;
import it.uniud.mads.jlibbig.core.Named;

/**
 * Objects created from this class are bigraphical controls describing the arity
 * (i.e. the number of ports) of nodes decorated with it. 
 * Every {@link Bigraph} has a {@link Signature}
 * describing the controls that can be assigned to its nodes; every {@link Node}
 * should be assigned exactly one control.
 */
public class Control extends AbstractNamed implements Named {

	protected int arity;

	/**
	 * Creates a control for the given arity and assign it a fresh
	 * name.
	 * 
	 * @param arity
	 *            a non-negative integer defining the number of ports of the
	 *            nodes decorated with this control.
	 */
	public Control(int arity) {
		this("C_" + NameGenerator.DEFAULT.generate(), arity);
	}

	/**
	 * Creates a control for the given name and arity
	 * 
	 * @param name
	 *            the name of the control.
	 * @param arity
	 *            a non-negative integer defining the number of ports of the
	 *            nodes decorated with this control.
	 */
	public Control(String name, int arity) {
		super(name);
		if(arity < 0)
			throw new IllegalArgumentException("Arity should be greater or equal to zero.");			
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
		if (arity != other.arity || !super.getName().equals(other.getName()))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getName() + ":" + arity;
	}
}
