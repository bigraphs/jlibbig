package jlibbig.core;

import java.util.*;

/**
 * Provides a base implementation of {@link Match}.  
 * 
 * @see Matcher
 */
public class AbstMatch<A extends AbstBigraph>  implements Match<A>{

	protected final A context;
	protected final A redex;
	protected final List<A> params;
	
	/**
	 * @param context
	 * @param redex
	 * @param params
	 */
	protected AbstMatch(A context, A redex, List<A> params){
		this.context = context;
		this.redex = redex;
		this.params = Collections.unmodifiableList(new  LinkedList<A>(params));
	}
		
	/**
	 * @see jlibbig.core.Match#getContext()
	 */
	@Override
	public A getContext() {
		return this.context;
	}

	/**
	 * @see jlibbig.core.Match#getRedex()
	 */
	@Override
	public A getRedex() {
		return this.redex;
	}

	/**
	 * @see jlibbig.core.Match#getParams()
	 */
	@Override
	public List<A> getParams() {
		return this.params;
	}
	
}
