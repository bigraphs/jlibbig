package it.uniud.mads.jlibbig.core.std;

import java.util.Collection;
import java.util.Iterator;

import it.uniud.mads.jlibbig.core.util.NameGenerator;
import it.uniud.mads.jlibbig.core.attachedProperties.*;

/**
 * Objects created from this class are bigraphical controls describing the
 * modality (i.e. active or passive) and the arity (i.e. the number of ports) of
 * nodes decorated with it. Every {@link Bigraph} has a {@link Signature}
 * describing the controls that can be assigned to its nodes; every {@link Node}
 * should be assigned exactly one control.
 */
public final class Control extends it.uniud.mads.jlibbig.core.Control implements PropertyTarget {

	private final boolean active;
	
	private final PropertyContainer props;
	
	/**
	 * Creates a control for the given arity and modality and assign it a fresh
	 * name.
	 * 
	 * @param active
	 *            a boolean indicating whether the control is active or passive.
	 * @param arity
	 *            a non-negative integer defining the number of ports of the
	 *            nodes decorated with this control.
	 */
	public Control(boolean active, int arity) {
		this("C_" + NameGenerator.DEFAULT.generate(), active, arity);
	}

	/**
	 * Creates a control for the given name, arity and modality.
	 * 
	 * @param name
	 *            the name of the control.
	 * @param active
	 *            a boolean indicating whether the control is active or passive.
	 * @param arity
	 *            a non-negative integer defining the number of ports of the
	 *            nodes decorated with this control.
	 */
	public Control(String name, boolean active, int arity) {
		super(name, arity);
		this.active = active;
		this.props = new PropertyContainer(this);
	}

	/**
	 * Checks if the control is active.
	 * 
	 * @return a boolean indicating whether the control is active or passive.
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
				|| !super.getName().equals(other.getName()))
			return false;
		return true;
	}

	String _tostring;
	
	@Override
	public String toString() {
		return this.toString(false);
	}
	
	public String toString(boolean includeProperties) {
		StringBuilder builder = null;
		if(_tostring == null){
			builder = new StringBuilder();
			builder.append(getName()).append(":(").append(getArity()).append((active) ? ",a)" : ",p)");
			_tostring = builder.toString();			
		}
		if(includeProperties){
			Collection<Property<?>> ps = props.getProperties();
			if(ps.isEmpty())
				return _tostring;
			if(builder == null){
				builder = new StringBuilder(_tostring);				
			}
			builder.append(" [");
			Iterator<Property<?>> ip = ps.iterator();
			while(ip.hasNext()){
				Property<?> p = ip.next();
				builder.append(p.getName()).append('=').append(p.get());
				if(ip.hasNext()){
					builder.append("; ");
				}else{
					builder.append(']');					
				}
			}
			return builder.toString();
		}else{
			return _tostring; 
		}
	}
	
	/* Properties handling */
	
	@Override
	public Property<?> attachProperty(Property<?> prop) {
		return props.attachProperty(prop);
	}

	@Override
	public <V> Property<V> detachProperty(Property<V> prop) {
		return props.detachProperty(prop);
	}

	@Override
	public <V> Property<V> detachProperty(String name) {
		return props.detachProperty(name);
	}

	@Override
	public <V> Property<V> getProperty(String name) {
		return props.getProperty(name);
	}

	@Override
	public Collection<Property<?>> getProperties() {
		return props.getProperties();
	}

	@Override
	public Collection<String> getPropertyNames() {
		return props.getPropertyNames();
	}

}