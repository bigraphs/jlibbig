package jlibbig.core;

import java.util.*;

/**
 * Describe a result of a match between a bigraph and a redex. 
 *
 * @param <A> type of bigraph
 */
public interface Match<A extends AbstBigraph> {
	/**
	 * Get the match's context
	 * @return the match's context
	 */
	A getContext();
	/**
	 * Get the match's redex
	 * @return the match's redex
	 */
	A getRedex();
	
	
	List<A> getParams();
}
