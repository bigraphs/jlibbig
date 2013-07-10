package jlibbig.core;

import java.util.Set;

/**
 * Handle: outername or edge
 *
 */
public interface Handle {
	/**
	 * Get a set of handle's points (innernames or ports).
	 */
	Set<? extends Point> getPoints();
}
