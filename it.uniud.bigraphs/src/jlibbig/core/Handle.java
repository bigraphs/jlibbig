package jlibbig.core;

import java.util.Collection;

import jlibbig.core.abstractions.Owned;

/**
 * Handle: outername or edge
 * 
 */
public interface Handle extends Owned, LinkEntity,
		jlibbig.core.abstractions.Handle {
	/**
	 * Get a set of handle's points (innernames or ports).
	 */
	@Override
	public abstract Collection<? extends Point> getPoints();

	public abstract EditableHandle getEditable();
}
