package it.uniud.mads.jlibbig.core.attachedProperties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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
	 * A flag indicating whether the property is writable
	 */
	protected boolean readOnly;

	private final List<PropertyListener<? super V>> _listeners = new LinkedList<>();

	/**
	 * A list holding all the listener registered for this property
	 */
	protected final Collection<PropertyListener<? super V>> listeners = Collections
			.unmodifiableCollection(_listeners);

	/**
	 * Every property shall have a name. The name can not be changed. However, a
	 * property can be referred by different names wrapping it inside other
	 * properties e.g. by means of {@link DelegatedProperty}.
	 *
	 * @param name
	 *            the name of the property.
	 */
	public Property(String name) {
		this(name,true,null);
	}
	public Property(String name, boolean writable, Collection<? extends PropertyListener<? super V>> listeners) {
		if (name == null)
			throw new IllegalArgumentException("The argument can not be null.");
		this.name = name;
		this.readOnly = !writable;
		if(listeners != null)
			this._listeners.addAll(listeners);
	}

	/**
	 * @return a boolean representing whether the property is read-only.
	 */
	public boolean isReadOnly() {
		return this.readOnly;
	}

	/**
	 * Checks if the given listener is registered.
	 *
	 * @param listener
	 *            the listener.
	 * @return a boolean representing whether the listener is actually
	 *         registered.
	 */
	public boolean isListenerRegistered(PropertyListener<? super V> listener) {
		return _listeners.contains(listener);
	}

	/**
	 * Registers the given listener with the property.
	 *
	 * @param listener
	 *            the listener to be registered.
	 */
	public void registerListener(PropertyListener<? super V> listener) {
		if (_listeners.contains(listener))
			return;
		_listeners.add(listener);
	}
	/**
	 * Unregisters the given listener.
	 *
	 * @param listener
	 *            the listener to be unregistered.
	 * @return a boolean representing whether the listener was actually
	 *         registered.
	 */
	public boolean unregisterListener(PropertyListener<? super V> listener) {
		return _listeners.remove(listener);
	}

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

	/**
	 * Raises the value changed event. The property parameter can be used to
	 * impersonate other properties (e.g. by delegation).
	 *
	 * @param property the property changed.
	 * @param oldValue the old value.
	 * @param newValue the new value.
	 */
	protected void tellChanged(Property<V> property, V oldValue, V newValue) {
		for(PropertyListener<? super V> listener : new ArrayList<>(listeners)){
			listener.onChanged(property, oldValue, newValue);
		}
	}
}