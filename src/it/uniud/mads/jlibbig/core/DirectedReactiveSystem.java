package it.uniud.mads.jlibbig.core;

import java.util.Collection;

/**
 * A bigraphical reactive system is a collection of states represented by
 * bigraphs that can evolve by means of reactions described by a collection of
 * reaction rules.
 * 
 * @param <B>
 *            the kind of bigraphs describing the states of the system.
 */
public interface DirectedReactiveSystem<B extends DirectedBigraph<?>> {
	public abstract Collection<? extends DirectedReactionRule<? extends B>> getRules();

	public abstract Collection<? extends B> getBigraphs();
}
