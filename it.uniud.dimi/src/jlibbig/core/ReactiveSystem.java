package jlibbig.core;

import java.util.Set;

import jlibbig.core.std.Signature;

public interface ReactiveSystem<B extends Bigraph<?>> {
	public abstract Signature getSignature();

	public abstract Set<? extends ReactionRule<? extends B>> getRules();

	public abstract Set<? extends B> getBigraphs();
}
