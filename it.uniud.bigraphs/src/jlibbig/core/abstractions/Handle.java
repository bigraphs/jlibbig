package jlibbig.core.abstractions;

import java.util.Collection;

/**
 * Handle: outername or edge
 *
 */
public interface Handle extends Owned, LinkEntity{
	/**
	 * Get a set of handle's points (innernames or ports).
	 */
	public abstract Collection<? extends Point> getPoints();
}
