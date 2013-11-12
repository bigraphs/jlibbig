package it.uniud.mads.jlibbig.core;

import java.util.*;

import it.uniud.mads.jlibbig.core.std.Signature;

/**
 * A rewriting system is a {@link ReactiveSystem} where reactions are described by 
 * means of rewriting rules.
 *  
 * @param <A> the kind of bigraphs describing the states of the system.
 * @param <B> the kind of bigraphs used by rewriting rules.
 */
public interface RewritingSystem<A extends Bigraph<?>, B extends Bigraph<?>>
		extends ReactiveSystem<A> {

	@Override
	public abstract Signature getSignature();

	@Override
	public abstract Collection<? extends RewritingRule<? extends A,? extends B>> getRules();

	@Override
	public abstract Collection<? extends A> getBigraphs();
}
