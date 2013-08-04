package jlibbig.core;

public interface ReactionRule<B extends AbstBigraph> {
	public Iterable<B> apply(B to);
}
