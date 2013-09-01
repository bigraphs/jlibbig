package jlibbig.core;

/**
 * Represents a match of a redex in a bigraph.
 * In particular, given a bigraph of type <code>A</code> G and a redex R, a match of R in G is a triple
 * <C,R,D> such that the composition C;R;D yields exactly G. The bigraph C is
 * called context, R redex and D parameter. 
 * 
 * @see BigraphMatcher
 * @param <A> type of bigraph
 */
public interface Match<A extends AbstractBigraph> {
	/**
	 * The match context.
	 * @return the context
	 */
	A getContext();
	/**
	 * The match redex.
	 * @return the redex
	 */
	A getRedex();
	
	
	/** The parameters of the match.
	 * @return the parameters
	 */
	A getParam();
}
