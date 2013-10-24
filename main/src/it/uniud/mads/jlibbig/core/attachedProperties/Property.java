package it.uniud.mads.jlibbig.core.attachedProperties;

/**
 * An attached property holds some value and can be dynamically attached (and
 * detached) to extend objects created from classes implementing the
 * {@link PropertyTarget} interface. A property may be attached to more than an
 * object at the same time. Changes of the value held by a property can be
 * observed by means of {@link PropertyListener}. The class that is interested
 * in processing a value change event implements the {@link PropertyListener}
 * interface, and the object created with that class is registered with an
 * object instance of a class inheriting from Property using
 * {@link #registerListener} method.
 * 
 * @param <V>
 *            the type of the value held by the property.
 */
public abstract class Property<V> {

	/**
	 * The property name.
	 */
	private final String name;

	/**
	 * Every property shall have a name. The name can not be changed. However, a
	 * property can be referred by different names wrapping it inside other
	 * properties e.g. by means of {@link DelegatedProperty}.
	 * 
	 * @param name
	 *            the name of the property.
	 */
	public Property(String name) {
		if (name == null)
			throw new IllegalArgumentException("The argument can not be null.");
		this.name = name;
	}

	/**
	 * @return a boolean representing whether the property is read-only.
	 */
	public abstract boolean isReadOnly();

	/**
	 * Checks if the given listener is registered.
	 * 
	 * @param listener
	 *            the listener.
	 * @return a boolean representing whether the listener is actually
	 *         registered.
	 */
	public abstract boolean isListenerRegistered(
			PropertyListener<? super V> listener);

	/**
	 * Registers the given listener with the property.
	 * 
	 * @param listener
	 *            the listener to be registered.
	 */
	public abstract void registerListener(PropertyListener<? super V> listener);

	/**
	 * Unregisters the given listener.
	 * 
	 * @param listener
	 *            the listener to be unregistered.
	 * @return a boolean representing whether the listener was actually
	 *         registered.
	 */
	public abstract boolean unregisterListener(
			PropertyListener<? super V> listener);

	/**
	 * Gets the property value.
	 * 
	 * @return the property value.
	 */
	public abstract V get();

	/**
	 * Sets the property value.
	 * 
	 * @param value
	 *            the new value.
	 * @return the old value.
	 */
	public abstract V set(V value);

	/**
	 * @return the property names.
	 */
	public final String getName() {
		return this.name;
	}

	/**
	 * The method is invoked when the property is attached to a property target
	 * handling properties with {@link PropertyContainer}.
	 * 
	 * @param target
	 *            the target to which the property is attached.
	 */
	protected void onAttach(PropertyTarget target) {
	};

	/**
	 * The method is invoked when the property is detached from a property
	 * target handling properties with {@link PropertyContainer}.
	 * 
	 * @param target
	 *            the target from which the property is detached.
	 */
	protected void onDetach(PropertyTarget target) {
	}

	@Override
	public String toString() {
		return "Property '" + getName() + "'=" + get();
	};

}