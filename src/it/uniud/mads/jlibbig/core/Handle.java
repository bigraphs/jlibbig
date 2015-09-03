package it.uniud.mads.jlibbig.core;

import java.util.Collection;

/**
 * Handles are link entities identifying the hyper-edges that compose link
 * graphs and that link points. Handles are outer names or edges depending on
 * whereas they belong to an outer interface or not.
 */
public interface Handle extends Owned, LinkEntity {
	/**
	 * Gets the collection of points (inner names or ports) linked by this
	 * handle.
	 */
	public abstract Collection<? extends Point> getPoints();
}
