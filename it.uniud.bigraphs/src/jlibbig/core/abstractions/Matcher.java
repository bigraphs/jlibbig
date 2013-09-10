package jlibbig.core.abstractions;


/**
 * Describes a Matcher for bigraphs.
 *
 * @param <A> type of the bigraph
 * @param <R> type of the redex
 */
public interface Matcher<A extends Bigraph<?>, R extends Bigraph<?>>  {
	/**
	 * Computes the matches of a redex into a bigraph of types
	 * <code>R</code> and <code>A</code> respectively.
	 * 
	 * @param agent the bigraph where to look for matches 
	 * @param redex the bigraph to look up for.
	 * @return an Iterable yielding every possible match.
	 */
	Iterable<? extends Match<? extends A>> match(A agent, R redex);
}
