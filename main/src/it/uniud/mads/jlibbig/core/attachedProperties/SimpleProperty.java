package it.uniud.mads.jlibbig.core.attachedProperties;

import java.util.*;

/**
*
* @param <V> the type of the value hold by the property.
*/
public class SimpleProperty<V> extends Property<V> {

	private V value;

	private List<PropertyListener<? super V>> _listeners = new LinkedList<>();

	/**
	 * A flag indicating whatever the property is writable
	 */
	protected boolean readOnly;

	/**
	 * A list holding all the listener registered for this property
	 */
	protected List<PropertyListener<? super V>> listeners = Collections
			.unmodifiableList(_listeners);
	
	@SafeVarargs
	public SimpleProperty(String name, PropertyListener<? super V>... listeners) {
		this(name,false,listeners);
	}
	
	public SimpleProperty(String name, Collection<? extends PropertyListener<? super V>> listeners) {
		this(name,false,listeners);
	}
	
	@SafeVarargs
	public SimpleProperty(String name, boolean writable, PropertyListener<? super V>... listeners) {
		this(name,writable,Arrays.asList(listeners));
	}
	
	public SimpleProperty(String name, boolean writable, Collection<? extends PropertyListener<? super V>> listeners) {
		super(name);
		this._listeners.addAll(listeners);
		this.readOnly = !writable;
	}

	@SafeVarargs
	public SimpleProperty(String name, V value,
			PropertyListener<? super V>... listeners) {
		this(name, value, false, listeners);
	}

	@SafeVarargs
	public SimpleProperty(String name, V value, boolean writable,
			PropertyListener<? super V>... listeners) {
		this(name,writable,listeners);
		this.value = value;
	}

	public SimpleProperty(String name, V value, boolean writable,
			Collection<? extends PropertyListener<? super V>> listeners) {
		this(name,writable,listeners);
		this.value = value;
	}
	
	@Override
	public boolean isReadOnly() {
		return this.readOnly;
	}

	@Override
	public boolean isListenerRegistered(PropertyListener<? super V> listener) {
		return _listeners.contains(listener);
	}

	@Override
	public void registerListener(PropertyListener<? super V> listener) {
		if (_listeners.contains(listener))
			return;
		_listeners.add(listener);
	}

	@Override
	public boolean unregisterListener(PropertyListener<? super V> listener) {
		return _listeners.remove(listener);
	}

	
	@Override
	public V get() {
		return value;
	}

	@Override
	public V set(V value) {
		if (isReadOnly())
			throw new UnsupportedOperationException("Property '" + getName()
					+ "' is read only.");
		return set(value, false);
	}

	protected V set(V value, boolean silent) {
		V old = this.value;
		this.value = value;
		if (!silent && old != value) {
			tellChanged(this, old, value);
		}
		return old;
	}
	

	protected void tellChanged(Property<V> property, V oldValue, V newValue) {
		ListIterator<PropertyListener<? super V>> li = _listeners
				.listIterator();
		while (li.hasNext()) {
			li.next().onChanged(property, oldValue, newValue);
		}
	}	
}
