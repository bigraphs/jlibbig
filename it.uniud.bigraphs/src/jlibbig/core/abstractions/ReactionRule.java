package jlibbig.core.abstractions;


public interface ReactionRule<B extends Bigraph<?>> {
	public Iterable<B> apply(B to);
}
