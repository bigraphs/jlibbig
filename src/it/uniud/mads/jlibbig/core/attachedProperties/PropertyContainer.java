package it.uniud.mads.jlibbig.core.attachedProperties;

import java.util.*;

/**
 * The class provides a default implementation of the {@link PropertyTarget}
 * interface. This class must be used to handle properties by classes
 * implementing the interface in order to make properties aware of the objects
 * to which are attached (see {@link Property#onAttach} and
 * {@link Property#onDetach}). The container requires a reference to the object
 * to which the properties are attached in order to enact it; if no reference is
 * provided, the container will use a reference to itself.
 */
public class PropertyContainer implements PropertyTarget {
	private final Map<String, Property<?>> props = new HashMap<>();

	private final PropertyTarget alias;

	/**
	 * The container requires a reference to the object to which the properties
	 * are attached in order to enact it; if no reference is provided, the
	 * container will use a reference to itself.
	 */
	public PropertyContainer() {
		this.alias = this;
	}

	/**
	 * The container requires a reference to the object to which the properties
	 * are attached in order to enact it; if no reference is provided, the
	 * container will use a reference to itself.
	 * 
	 * @param alias
	 *            the object the container enacts.
	 */
	public PropertyContainer(PropertyTarget alias) {
		this.alias = (alias == null) ? this : alias;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.uniud.mads.jlibbig.core.AttachedPropertyTarget#attachProperty(jlibbig
	 * .core. AttachedProperty)
	 */
	@Override
	public Property<?> attachProperty(Property<?> prop) {
		String name = prop.getName();
		Property<?> old = this.props.get(name);
		if (old != null)
			old.onDetach(this.alias);
		prop.onAttach(this.alias);
		return this.props.put(prop.getName(), prop);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.uniud.mads.jlibbig.core.AttachedPropertyTarget#detachProperty(jlibbig
	 * .core. AttachedProperty)
	 */
	@Override
	public <V> Property<V> detachProperty(Property<V> prop) {
		if (props.containsValue(prop))
			return detachProperty(prop.getName());
		else
			return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.uniud.mads.jlibbig.core.AttachedPropertyTarget#detachProperty(java
	 * .lang.String)
	 */
	@Override
	public <V> Property<V> detachProperty(String name) {
		@SuppressWarnings("unchecked")
		Property<V> prop = (Property<V>) props.remove(name);
		if (prop != null)
			prop.onDetach(this.alias);
		return prop;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.uniud.mads.jlibbig.core.AttachedPropertyTarget#getProperty(java.lang
	 * .String)
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
	 * it.uniud.mads.jlibbig.core.AttachedPropertyTarget#getProperty(java.lang
	 * .String)
	 */
	@Override
	public Collection<Property<?>> getProperties() {
		return props.values();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.uniud.mads.jlibbig.core.AttachedPropertyTarget#getPropertyNames(java
	 * .lang.String)
	 */
	@Override
	public Collection<String> getPropertyNames() {
		return this.props.keySet();
	}
}
