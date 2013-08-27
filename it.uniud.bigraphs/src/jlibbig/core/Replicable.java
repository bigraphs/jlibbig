package jlibbig.core;

public interface Replicable {

	public abstract void registerListener(ReplicateListener listener);

	public abstract boolean unregisterListener(ReplicateListener listener);

}