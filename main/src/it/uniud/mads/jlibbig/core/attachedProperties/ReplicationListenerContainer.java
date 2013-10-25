package it.uniud.mads.jlibbig.core.attachedProperties;

import java.util.*;

/**
 * The class provides an helper for implementing {@link Replicating} interface.
 * That interface allows the implementer to make its replications observable to
 * implementations of {@link ReplicatingListener}.
 */
public class ReplicationListenerContainer {

	/**
	 * The collection holding all registered listeners
	 */
	private Collection<ReplicationListener> _listeners = new LinkedList<>();
	/**
	 * The read-only collection of registered listeners exposed to inheriting
	 * classes.
	 */
	protected Collection<ReplicationListener> listeners = Collections
			.unmodifiableCollection(_listeners);

	/**
	 * Creates the container and registers the given listeners.
	 * 
	 * @param listeners
	 *            the listeners to be registered.
	 */
	public ReplicationListenerContainer(ReplicationListener... listeners) {
		this._listeners.addAll(Arrays.asList(listeners));
	}

	/**
	 * Registers a replication listener to the container.
	 * 
	 * @param listener
	 *            the listener to be registered.
	 */
	public void registerListener(ReplicationListener listener) {
		if (_listeners.contains(listener))
			return;
		_listeners.add(listener);
	}

	/**
	 * Unregisters a replication listener to the container.
	 * 
	 * @param listener
	 *            the listener to be unregistered.
	 * @return a boolean representing whether the listener was actually
	 *         registered.
	 */
	public boolean unregisterListener(ReplicationListener listener) {
		return _listeners.remove(listener);
	}


	/**
	 * Checks if the given listener is registered.
	 * 
	 * @param listener
	 *            the listener.
	 * @return a boolean representing whether the listener is actually
	 *         registered.
	 */
	public boolean isListenerRegistered(ReplicationListener listener) {
		return _listeners.contains(listener);
	}
	
	/**
	 * Tells the listeners that a replication happened.
	 * @param original
	 *            the original instance.
	 * @param copy
	 *            the replica.
	 */
	public void tellReplicated(Replicating original, Replicating copy) {
		for(ReplicationListener listener : new ArrayList<>(_listeners)){
			listener.onReplicated(original, copy);
		}
	}

}
