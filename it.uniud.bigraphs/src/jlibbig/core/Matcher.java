package jlibbig.core;

/**
 * Describes a Matcher for bigraphs.
 *
 * @param <A> type of the bigraph
 * @param <R> type of the redex
 */
public interface Matcher<A extends AbstBigraph, R extends AbstBigraph>  {
	/**
	 * Perform a match.
	 * @param agent Bigraph checked.
	 * @param redex Redex used for the match.
	 * @return an Iterable over the results of the match.
	 */
	Iterable<Match<A>> match(A agent, R redex);
}
