package jlibbig.core.attachedProperties;

import jlibbig.core.*;

/**
 * A class for properties to be shared between targets implementing
 * {@link jlibbig.core.Replicable}. The class automatically attach itself to new
 * replicas thus rendering the wrapped property shared between these. Listeners
 * are registered to the wrapped property. The class can be attached also to
 * targets not implementing Replicable, but the automatic sharing behaviour will
 * not be enabled (on these).
 * 
 * @see ReplicatingProperty
 * @param <V>
 */
public class SharedProperty<V> extends Property<V> {

	protected final Property<V> property;

	protected final ReplicateListener listener = new ReplicateListener() {
		@Override
		public void onReplicate(Replicable original, Replicable copy) {
			SharedProperty.this.onReplicate(original, copy);
		}
	};

	/**
	 * @param property
	 *            the property to be wrapped.
	 */
	public SharedProperty(Property<V> property) {
		if (property == null)
			throw new IllegalArgumentException(
					"The encapsulated property can not be null.");
		this.property = property;
	}

	/**
	 * @return the wrapped property.
	 */
	public Property<V> getProperty() {
		return this.property;
	}

	@Override
	public boolean isReadOnly() {
		return this.property.isReadOnly();
	}

	@Override
	public void registerListener(PropertyListener<? super V> listener) {
		this.property.registerListener(listener);
	}

	@Override
	public boolean isListenerRegistered(PropertyListener<? super V> listener) {
		return this.property.isListenerRegistered(listener);
	}

	@Override
	public boolean unregisterListener(PropertyListener<? super V> listener) {
		return this.property.unregisterListener(listener);
	}

	@Override
	public V get() {
		return this.property.get();
	}

	@Override
	public V set(V value) {
		return this.property.set(value);
	}

	@Override
	public String getName() {
		return this.property.getName();
	}

	@Override
	protected void onAttach(PropertyTarget target) {
		this.property.onAttach(target);
		if (target instanceof Replicable)
			((Replicable) target).registerListener(listener);
	}

	@Override
	protected void onDetach(PropertyTarget target) {
		this.property.onDetach(target);
		if (target instanceof Replicable)
			((Replicable) target).unregisterListener(listener);
	}

	/**
	 * This method is called when a PropertyTarget holding the property is
	 * replicated in order to attach this property to the replica and listen for
	 * its replications. Inherit this method to intercept replications and
	 * filter automatic registration of ReplicateListeners
	 * {@link SharedProperty.listener} and attachment of this property.
	 * 
	 * @param original
	 * @param copy
	 */
	protected void onReplicate(Replicable original, Replicable copy) {
		((PropertyTarget) copy).attachProperty(SharedProperty.this);
		copy.registerListener(listener);
	};

}
