package jlibbig.core;

interface Replicable {
	/**
	 * replicate an entity
	 * @return the entity's duplicate
	 */
	public abstract Replicable replicate();
	
	public abstract void registerListener(ReplicateListener listener);
	
	public abstract boolean unregisterListener(ReplicateListener listener);
}
