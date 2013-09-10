package jlibbig.core.attachedProperties;

import java.util.*;

public class SimpleProperty<V> extends Property<V> {

	private V value;
	private final String name;

	private List<PropertyListener<? super V>> _listeners = new LinkedList<>();

	/**
	 * A list holding all the listener registered for this property
	 */
	protected List<PropertyListener<? super V>> listeners = Collections
			.unmodifiableList(_listeners);

	/**
	 * A flag indicating whatever the property is writable
	 */
	protected boolean readOnly = true;

	@SafeVarargs
	public SimpleProperty(String name, PropertyListener<? super V>... listeners) {
		this.name = name;
		this._listeners.addAll(Arrays.asList(listeners));
	}

	@SafeVarargs
	public SimpleProperty(String name, V value,
			PropertyListener<? super V>... listeners) {
		this(name, value, false, listeners);
	}

	@SafeVarargs
	public SimpleProperty(String name, V value, boolean readOnly,
			PropertyListener<? super V>... listeners) {
		this.name = name;
		this.value = value;
		this.readOnly = readOnly;
		this._listeners.addAll(Arrays.asList(listeners));
	}

	public SimpleProperty(String name, V value, boolean writable,
			Collection<? extends PropertyListener<? super V>> listeners) {
		this.name = name;
		this.value = value;
		this.readOnly = writable;
		this._listeners.addAll(listeners);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jlibbig.core.AttachedProperty#isReadOnly()
	 */
	@Override
	public boolean isReadOnly() {
		return this.readOnly;
	}

	@Override
	public boolean isListenerRegistered(PropertyListener<? super V> listener) {
		return _listeners.contains(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jlibbig.core.AttachedProperty#registerListener(jlibbig.core.
	 * AttachedPropertyListener)
	 */
	@Override
	public void registerListener(PropertyListener<? super V> listener) {
		if (_listeners.contains(listener))
			return;
		_listeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jlibbig.core.AttachedProperty#unregisterListener(jlibbig.core.
	 * AttachedPropertyListener)
	 */
	@Override
	public boolean unregisterListener(PropertyListener<? super V> listener) {
		return _listeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jlibbig.core.AttachedProperty#get()
	 */
	@Override
	public V get() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jlibbig.core.AttachedProperty#set(V)
	 */
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
		if (!silent) {
			tellChanged(this, old, value);
		}
		return old;
	}

	protected void tellChanged(Property<V> property, V oldValue, V newValue) {
		ListIterator<PropertyListener<? super V>> li = _listeners
				.listIterator();
		while (li.hasNext()) {
			li.next().onChange(property, oldValue, newValue);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jlibbig.core.AttachedProperty#getName()
	 */
	@Override
	public String getName() {
		return this.name;
	}

}
