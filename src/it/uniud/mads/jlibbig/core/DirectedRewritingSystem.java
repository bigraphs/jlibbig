package it.uniud.mads.jlibbig.core;

import java.util.*;

/**
 * A rewriting system is a {@link DirectedReactiveSystem} where reactions are 
 * described by means of rewriting rules.
 *  
 * @param <A> the kind of bigraphs describing the states of the system.
 * @param <B> the kind of bigraphs used by rewriting rules.
 */
public interface DirectedRewritingSystem<A extends DirectedBigraph<?>, 
		B extends DirectedBigraph<?>> extends DirectedReactiveSystem<A> {

	@Override
	public abstract Collection<? extends DirectedRewritingRule<? extends A,
		? extends B>> getRules();

	@Override
	public abstract Collection<? extends A> getBigraphs();
}
