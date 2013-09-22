package it.uniud.mads.jlibbig.core.attachedProperties;

public interface ReplicateListener {
	public abstract void onReplicate(Replicable original, Replicable copy);
}
