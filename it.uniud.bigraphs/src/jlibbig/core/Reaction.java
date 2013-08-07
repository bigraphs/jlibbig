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
	
	/**
	 * Create a new reaction.
	 * @param redex
	 * 			Reaction's redex.
	 * @param reactum
	 * 			Reaction's reactum.
	 * @throws IllegalArgumentException
	 */
	public Reaction( R redex , R reactum ) throws IllegalArgumentException{
		if( redex == null || reactum == null )
			throw new IllegalArgumentException( "While initializing a Reaction, both Redex and Reactum can't be null." );
		if(redex.getSignature() != reactum.getSignature() )
			throw new IllegalArgumentException("Redex and Reactum must share the same signature.");
		this.redex = redex;
		this.reactum = reactum;
	}
	
	/**
	 * Get the reaction's redex.
	 */
	public R getRedex(){
		return redex;
	}
	
	/**
	 * Get the reaction's reactum.
	 */
	public R getReactum(){
		return reactum;
	}
	
	/**
	 * Get the signature of both redex and reactum.
	 * @return
	 * 			The reaction's signature.
	 */
	public Signature getSignature(){
		return redex.getSignature();
	}
}
