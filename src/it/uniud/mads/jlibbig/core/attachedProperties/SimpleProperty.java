package it.uniud.mads.jlibbig.core.attachedProperties;

import java.util.*;

/**
* The class provides a basic concretization of {@link Property}. It can operate
* as it is and can take care of values and listeners for inheriting classes.
* 
* @param <V> the type of the value hold by the property.
*/
public class SimpleProperty<V> extends Property<V> {

	private V value;

	
	@SafeVarargs
	public SimpleProperty(String name, PropertyListener<? super V>... listeners) {
		this(name,false,listeners);
	}
		
	@SafeVarargs
	public SimpleProperty(String name, boolean writable, PropertyListener<? super V>... listeners) {
		this(name,writable,Arrays.asList(listeners));
	}
	
	public SimpleProperty(String name, boolean writable, Collection<? extends PropertyListener<? super V>> listeners) {
		super(name,writable,listeners);
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

	/**
	 * This method allows to set the property value without raising any event.
	 * 
	 * @param value the new value.
	 * @param silent if true the change will not be listened.
	 * @return the old value.
	 */
	protected V set(V value, boolean silent) {
		V old = this.value;
		this.value = value;
		if (!silent && old != value) {
			tellChanged(this, old, value);
		}
		return old;
	}	
}
