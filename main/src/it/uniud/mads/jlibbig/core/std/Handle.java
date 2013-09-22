package it.uniud.mads.jlibbig.core.std;

import java.util.Collection;

import it.uniud.mads.jlibbig.core.Owned;

/**
 * Handle: outername or edge
 * 
 */
public interface Handle extends Owned, LinkEntity,
		it.uniud.mads.jlibbig.core.Handle {
	/**
	 * Get a set of handle's points (innernames or ports).
	 */
	@Override
	public abstract Collection<? extends Point> getPoints();

	public abstract EditableHandle getEditable();
}
