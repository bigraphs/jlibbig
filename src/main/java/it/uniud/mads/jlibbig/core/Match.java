package it.uniud.mads.jlibbig.core;

/**
 * This interface describe the basic match of a bigraph in another one. In
 * particular, a match of a bigraph R in G is a triple &lt;C,R,P&gt; yielding G when
 * composed. The bigraphs C, R, and P are called Context, Redex and Prameter of
 * the match respectively.
 * 
 * @param <A>
 *            the kind of bigraph the match belongs.
 */
public interface Match<A extends Bigraph<?>> {
	/**
	 * The match context.
	 * 
	 * @return a bigraph representing the context of this match.
	 */
	A getContext();

	/**
	 * The match redex.
	 * 
	 * @return a bigraph representing the redex of this match.
	 */
	A getRedex();

	/**
	 * The parameter of the match.
	 * 
	 * @return a bigraph representing the parameter of this match.
	 */
	A getParam();
}
