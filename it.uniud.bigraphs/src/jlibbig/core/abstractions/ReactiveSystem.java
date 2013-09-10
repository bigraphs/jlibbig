package jlibbig.core.abstractions;

import java.util.Set;

import jlibbig.core.Signature;

public interface ReactiveSystem<B extends Bigraph<?>> {
	public abstract Signature getSignature();

	public abstract Set<? extends ReactionRule<? extends B>> getRules();

	public abstract Set<? extends B> getBigraphs();
}
