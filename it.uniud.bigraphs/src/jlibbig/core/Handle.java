package jlibbig.core;

import java.util.Collection;

/**
 * Handle: outername or edge
 *
 */
public interface Handle extends Owned{
	/**
	 * Get a set of handle's points (innernames or ports).
	 */
	Collection<? extends Point> getPoints();
}
