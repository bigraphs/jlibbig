package it.uniud.mads.jlibbig.core.attachedProperties;

/**
 * The observable interface offers methods listening object replications by
 * means of {@link ReplicationListener}. The class that is interested in
 * processing a replication event implements the {@link ReplicationListener}
 * interface, and the object created with that class is registered with an
 * object instance of a class implementing the Replicating interface using
 * registerReplicatingListener method. An helper for implementing this interface
 * is provided by {@link ReplicationListenerContainer}.
 */
public interface Replicating {

	/**
	 * Registers the given replication listener to the replicating instance.
	 * 
	 * @param listener
	 *            the listener to be registered.
	 */
	public abstract void registerListener(ReplicationListener listener);

	/**
	 * Unregisters the given replication listener.
	 * 
	 * @param listener
	 *            the listener to be unregistered.
	 * @return a boolean representing whether the listener was actually
	 *         registered
	 */
	public abstract boolean unregisterListener(ReplicationListener listener);

}