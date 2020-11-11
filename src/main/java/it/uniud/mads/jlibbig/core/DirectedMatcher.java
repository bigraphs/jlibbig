package it.uniud.mads.jlibbig.core;

/**
 * Provides services for computing the matches of a bigraph (sometimes called redex) 
 * into an other. In particular, a match of a bigraph R in G is a triple &lt;C,R,P&gt;
 * yielding G when composed. The bigraphs C, R, and P are called Context, Redex and Prameter of
 * the match respectively. Matches are described by implementations of {@link Match}.
 * 
 * @param <A>
 *            kind of the 'agent' bigraph.
 * @param <R>
 *            kind of the redex.
 */
public interface DirectedMatcher<A extends DirectedBigraph<?>, R extends DirectedBigraph<?>> {
	/**
	 * Computes the matches of a redex into a bigraph of types <code>R</code>
	 * and <code>A</code> respectively.
	 * 
	 * @param agent
	 *            the bigraph where to look for matches
	 * @param redex
	 *            the bigraph to look up for.
	 * @return an Iterable yielding every possible match.
	 */
	Iterable<? extends DirectedMatch<? extends A>> match(A agent, R redex);
}
