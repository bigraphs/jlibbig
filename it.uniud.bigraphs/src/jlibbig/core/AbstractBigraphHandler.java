package jlibbig.core;

import java.util.*;

/**
 * Interface implemented by every bigraph's class
 *
 */
public interface AbstractBigraphHandler extends Owner{
		/**
		 * Get the bigraph's signature
		 * @return the bigraph's signature
		 */
		public abstract Signature getSignature();
		
		/**
		 * check if the bigraph is empty (has no roots, sites, inner and outer names)
		 * @return a boolean value (true if the bigraph is empty)
		 */
		public abstract boolean isEmpty();
		
		/**
		 * Check if the bigraph is ground (no sites nor inner names)
		 * @return a boolean value (true if ground)
		 */
		public abstract boolean isGround();

		/**
		 * Get bigraph's roots.
		 * @return a list carrying bigraph's roots
		 */
		public abstract List<? extends Root> getRoots();

		/**
		 * Get bigraph's sites.
		 * @return a list carrying bigraph's sites
		 */
		public abstract List<? extends Site> getSites();

		/**
		 * Get bigraph's outer names.
		 * @return a list carrying bigraph's outer names
		 */
		public abstract Collection<? extends OuterName> getOuterNames();

		/**
		 * Get bigraph's inner names.
		 * @return a list carrying bigraph's inner names
		 */
		public abstract Collection<? extends InnerName> getInnerNames();

		/**
		 * Get bigraph's nodes.
		 * @return a set containing bigraph's nodes.
		 */
		public abstract Set<? extends Node> getNodes();

		/**
		 * Get bigraph's edges.
		 * @return a set containing bigraph's edges.
		 */
		public abstract Set<? extends Edge> getEdges();
}
