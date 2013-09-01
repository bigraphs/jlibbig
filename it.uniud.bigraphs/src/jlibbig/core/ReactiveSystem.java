package jlibbig.core;

import java.util.Set;

public interface ReactiveSystem<B extends AbstractBigraph> {
	public abstract Signature getSignature();
	public abstract Set<? extends ReactionRule<? extends B>> getRules();
	public abstract Set<? extends B> getBigraphs();
}
