package it.uniud.mads.jlibbig.core.attachedProperties;

import java.util.*;

public class PropertyContainer implements PropertyTarget {
	private final Map<String, Property<?>> props = new HashMap<>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.uniud.mads.jlibbig.core.AttachedPropertyTarget#attachProperty(jlibbig.core.
	 * AttachedProperty)
	 */
	@Override
	public Property<?> attachProperty(Property<?> prop) {
		String name = prop.getName();
		Property<?> old = this.props.get(name);
		if (old != null)
			old.onDetach(this);
		prop.onAttach(this);
		return this.props.put(prop.getName(), prop);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.uniud.mads.jlibbig.core.AttachedPropertyTarget#detachProperty(jlibbig.core.
	 * AttachedProperty)
	 */
	@Override
	public <V> Property<V> detachProperty(Property<V> prop) {
		return detachProperty(prop.getName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.uniud.mads.jlibbig.core.AttachedPropertyTarget#detachProperty(java.lang.String)
	 */
	@Override
	public <V> Property<V> detachProperty(String name) {
		@SuppressWarnings("unchecked")
		Property<V> prop = (Property<V>) props.remove(name);
		if (prop != null)
			prop.onDetach(this);
		return prop;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.uniud.mads.jlibbig.core.AttachedPropertyTarget#getProperty(java.lang.String)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <V> Property<V> getProperty(String name) {
		return (Property<V>) props.get(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.uniud.mads.jlibbig.core.AttachedPropertyTarget#getPropertyNames(java.lang.String)
	 */
	@Override
	public Set<String> getPropertyNames() {
		return this.props.keySet();
	}

}
