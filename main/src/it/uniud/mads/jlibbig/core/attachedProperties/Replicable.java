package it.uniud.mads.jlibbig.core.attachedProperties;

public interface Replicable {

	public abstract void registerListener(ReplicateListener listener);

	public abstract boolean unregisterListener(ReplicateListener listener);

}