package jlibbig.core;

/**
 * Provides a base implementation of {@link Match}.  
 * 
 * @see Matcher
 */
public class AbstMatch<A extends AbstBigraph>  implements Match<A>{

	protected final A context;
	protected final A redex;
	//protected final List<A> params;
	protected final A param;
	
	/**
	 * @param context
	 * @param redex
	 * @param param
	 */
	protected AbstMatch(A context, A redex, A param){
		this.context = context;
		this.redex = redex;
		//this.params = Collections.unmodifiableList(new  LinkedList<A>(params));
		this.param = param;
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
	 * @see jlibbig.core.Match#getParam()
	 */
	@Override
	public A getParam() {
		return this.param;
	}
	
}
