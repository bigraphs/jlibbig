package it.uniud.mads.jlibbig.core.std;

import it.uniud.mads.jlibbig.core.attachedProperties.Replicating;

interface Replicable extends Replicating {
	public abstract Replicating replicate();
}
