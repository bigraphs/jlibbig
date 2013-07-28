package jlibbig.core;
/**
 * @see jlibbig.core.AbstReaction
 *
 * @param <R>
 * 			Bigraph's type used to represent a reaction.
 */
public class Reaction< R extends AbstBigraph> implements AbstReaction<R>{
	R redex;
	R reactum;
	public Reaction( R redex , R reactum ){
		this.redex = redex;
		this.reactum = reactum;
	}
	
	public R getRedex(){
		return redex;
	}
	
	public R getReactum(){
		return reactum;
	}
}
