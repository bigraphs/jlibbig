package it.uniud.mads.jlibbig.core.attachedProperties;

/**
 * The listener interface for receiving events regarding the change of property
 * values. The class that is interested in processing a value change event
 * implements this interface, and the object created with that class is
 * registered with an instance of {@link Property}, using the
 * {@link Property#registerListener} method. When the event occurs, the object's
 * onChanged method is invoked.
 * 
 * @param <V>
 *            the type of the value hold by the property.
 */
public interface PropertyListener<V> {
	/**
	 * This method is invoked any time the value of a property to which the
	 * listener is registered changes. The invocation happens after the new
	 * value has been written.
	 * 
	 * @param property
	 *            the property whose value changed.
	 * @param oldValue
	 *            the old value.
	 * @param newValue
	 *            the new value.
	 * 
	 * @see Property
	 */
	public abstract void onChanged(Property<? extends V> property, V oldValue,
			V newValue);

}
