package it.uniud.mads.jlibbig.core.std;

import it.uniud.mads.jlibbig.core.attachedProperties.Replicable;

interface ReplicableEx extends Replicable {
	public abstract Replicable replicate();
}
