package jlibbig.core;

/**
 * Interface for bigraph's reactions.
 *
 * @param <R>
 * 			Bigraph's type used to represent a reaction.
 */
public interface AbstReaction<R extends AbstBigraph>{
	/**
	 * Get the redex of the reaction.
	 * @return
	 * 			Reaction's redex.
	 */
	public R getRedex();
	
	/**
	 * Get the reactum of the reaction.
	 * @return
	 * 			Reaction's reactum.
	 */
	public R getReactum();
}
