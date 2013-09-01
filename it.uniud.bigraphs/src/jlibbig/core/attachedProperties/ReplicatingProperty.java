package jlibbig.core.attachedProperties;

import java.util.*;
import jlibbig.core.*;
/**
 * A class for properties attached to targets implementing 
 * {@link jlibbig.core.Replicable}.  The class automatically attach a copy of 
 * itself to new replicas thus. Listeners are registered also for this new copy.
 * Therefore, functionality provided is similar to {@link SharedProperty}, but
 * results in a copy-on-write behaviour across the replicas.
 * The class can be attached also to targets not implementing Replicable, but 
 * the automatic sharing behaviour will not be enabled (on these).
 * 
 * @param <V>
 */
public class ReplicatingProperty<V> extends SimpleProperty<V> {

	protected final ReplicateListener repListener = new ReplicateListener() {
		public void onReplicate(Replicable original, Replicable copy) {
			ReplicatingProperty.this.onReplicate(original, copy);
		}
	};

	public ReplicatingProperty(String name,
			@SuppressWarnings("unchecked") PropertyListener<V>... listeners) {
		super(name, listeners);
	}

	public ReplicatingProperty(String name, V value,
			@SuppressWarnings("unchecked") PropertyListener<V>... listeners) {
		this(name, value, true, listeners);
	}

	public ReplicatingProperty(String name, V value, boolean writable,
			@SuppressWarnings("unchecked") PropertyListener<V>... listeners) {
		super(name, value, writable, listeners);
	}

	public ReplicatingProperty(String name, V value, boolean writable,
			Collection<PropertyListener<V>> listeners) {
		super(name, value, writable, listeners);
	}

	@Override
	protected void onAttach(PropertyTarget target) {
		if (target instanceof Replicable)
			((Replicable) target).registerListener(repListener);
	}

	@Override
	protected void onDetach(PropertyTarget target) {
		if (target instanceof Replicable)
			((Replicable) target).unregisterListener(repListener);
	}

	/**
	 * This method is called when a PropertyTarget holding the property is
	 * replicated in order to attach a copy of this property to the replica and
	 * listen for its replications. Inherit this method to intercept
	 * replications and filter automatic registration of ReplicateListeners
	 * {@link SharedProperty.listener} and attachment of this property.
	 * 
	 * @param original
	 * @param copy
	 */
	protected void onReplicate(Replicable original, Replicable copy) {
		((PropertyTarget) copy).attachProperty(new ReplicatingProperty<V>(this
				.getName(), this.get(), this.isReadOnly(), super.listeners));
	};
}
