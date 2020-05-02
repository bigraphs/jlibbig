package it.uniud.mads.jlibbig.core.attachedProperties;

import java.util.*;

/**
 * A class for properties attached to targets implementing
 * {@link Replicating}. The class automatically attach a copy of
 * itself to new replicas thus. Listeners are registered also for this new copy.
 * Therefore, functionality provided is similar to {@link SharedProperty}, but
 * results in a copy-on-write behavior across the replicas. The class can be
 * attached also to targets not implementing Replicating, but the automatic
 * sharing behavior will not be enabled (on these).
 * 
 * @param <V>
 *            the type of the value held by the property.
 */
public class ReplicatingProperty<V> extends SimpleProperty<V> {

	protected final ReplicationListener repListener = new ReplicationListener() {
		@Override
		public void onReplicated(Replicating original, Replicating copy) {
			ReplicatingProperty.this.onReplicated(original, copy);
		}
	};

	@SafeVarargs
	public ReplicatingProperty(String name,
			PropertyListener<? super V>... listeners) {
		super(name, listeners);
	}

	@SafeVarargs
	public ReplicatingProperty(String name, V value,
			PropertyListener<? super V>... listeners) {
		this(name, value, true, listeners);
	}

	@SafeVarargs
	public ReplicatingProperty(String name, V value, boolean writable,
			PropertyListener<? super V>... listeners) {
		super(name, value, writable, listeners);
	}

	public ReplicatingProperty(String name, V value, boolean writable,
			Collection<PropertyListener<? super V>> listeners) {
		super(name, value, writable, listeners);
	}

	@Override
	protected void onAttach(PropertyTarget target) {
		if (target instanceof Replicating)
			((Replicating) target).registerListener(repListener);
	}

	@Override
	protected void onDetach(PropertyTarget target) {
		if (target instanceof Replicating)
			((Replicating) target).unregisterListener(repListener);
	}

	/**
	 * This method is called when a PropertyTarget holding the property is
	 * replicated in order to attach a copy of this property to the replica and
	 * listen for its replications. Inherit this method to intercept
	 * replications and filter automatic registration of ReplicationListeners
	 * {@link SimpleProperty#listeners} and attachment of this property.
	 * 
	 * @param original
	 * @param copy
	 */
	protected void onReplicated(Replicating original, Replicating copy) {
		((PropertyTarget) copy).attachProperty(new ReplicatingProperty<>(this
				.getName(), this.get(), this.isReadOnly(), super.listeners));
	};
}
