package it.uniud.mads.jlibbig.core.attachedProperties;

/**
 * The listener interface for receiving replication events. The class that is
 * interested in processing a replication event implements this interface, and the
 * object created with that class is registered with an instance of
 * {@link Replicating}, using the replicating's registerReplicatingListener
 * method. When the event occurs, the object's onReplicated method is invoked.
 */
public interface ReplicationListener {
	/**
	 * This method is invoked any time a replicating instance to which the
	 * listener is registered replicates.
	 * 
	 * @param original
	 *            the original instance.
	 * @param copy
	 *            the replica.
	 * @see Replicating
	 */
	public abstract void onReplicated(Replicating original, Replicating copy);
}
