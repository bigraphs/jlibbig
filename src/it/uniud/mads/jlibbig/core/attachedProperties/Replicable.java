package it.uniud.mads.jlibbig.core.attachedProperties;


/**
 * This interface characterises classes whose instances are able to
 * self-replicate when {@link #replicate} is invoked. The contract of
 * {@link Replicating} must be honoured after any successful replication
 */
public interface Replicable extends Replicating {
	/**
	 * Replicates this object and triggers the suitable events as specified by
	 * {@link Replicating}.
	 * 
	 * @return a replica of this object
	 */
	public abstract Replicating replicate();
}
