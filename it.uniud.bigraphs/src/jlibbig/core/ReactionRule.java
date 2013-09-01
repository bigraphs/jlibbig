package jlibbig.core;

public interface ReactionRule<B extends AbstractBigraph> {
	public Iterable<B> apply(B to);
}
