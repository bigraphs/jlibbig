package it.uniud.mads.jlibbig.core.attachedProperties;

/**
 * A class for properties to be shared between targets implementing
 * {@link Replicating}. The class automatically attach itself to new replicas
 * thus rendering the wrapped property shared between these. Listeners are
 * registered to the wrapped property. The class can be attached also to targets
 * not implementing {@link Replicating}, but the automatic sharing behavior
 * will not be enabled (on these).
 * 
 * @see ReplicatingProperty
 * @param <V> the type of the value hold by the property.
 */
public class SharedProperty<V> extends Property<V> {

	protected final Property<V> property;

	protected final ReplicationListener listener = new ReplicationListener() {
		@Override
		public void onReplicated(Replicating original, Replicating copy) {
			SharedProperty.this.onReplicated(original, copy);
		}
	};

	/**
	 * Gets the name of the given property. Throws an
	 * {@link IllegalArgumentException} if the property is null.
	 * 
	 * @param property
	 *            the property to query.
	 * @return the property name.
	 */
	private static String retrieveName(Property<?> property) {
		if (property == null)
			throw new IllegalArgumentException(
					"The encapsulated property can not be null.");
		return property.getName();
	}

	/**
	 * @param property
	 *            the property to be wrapped.
	 */
	public SharedProperty(Property<V> property) {
		super(retrieveName(property));
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
	protected void onAttach(PropertyTarget target) {
		this.property.onAttach(target);
		if (target instanceof Replicating)
			((Replicating) target).registerListener(listener);
	}

	@Override
	protected void onDetach(PropertyTarget target) {
		this.property.onDetach(target);
		if (target instanceof Replicating)
			((Replicating) target).unregisterListener(listener);
	}

	/**
	 * This method is called when a PropertyTarget holding the property is
	 * replicated in order to attach this property to the replica and listen for
	 * its replications. Inherit this method to intercept replications and
	 * filter automatic registration of ReplicateListeners
	 * {@link SharedProperty#listener} and attachment of this property.
	 * 
	 * @param original
	 * @param copy
	 */
	protected void onReplicated(Replicating original, Replicating copy) {
		((PropertyTarget) copy).attachProperty(SharedProperty.this);
		copy.registerListener(listener);
	};

}
