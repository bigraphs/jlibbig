package jlibbig.core;

import java.util.*;

/**
 * Represents a match of a redex in a bigraph.
 * In particular, given a bigraph of type <code>A</code> G and a redex R, a match of R in G is a triple
 * <C,R,D> such that the composition C;R;D yields exactly G. The bigraph C is
 * called context, R redex and the prime bigraphs in D parameters. In fact, D is
 * given as a list of prime bigraphs, one for each site of R. These may have 
 * overlapping interfaces, therefore use a suitable operation to compose them
 * e.g. {@link BigraphBuilder#leftParallelProduct}.  
 * 
 * @see BigraphMatcher
 * @param <A> type of bigraph
 */
public interface Match<A extends AbstBigraph> {
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
	
	
	/** The parameters of the match indexed over redex sites.
	 * @return
	 */
	List<A> getParams();
}
