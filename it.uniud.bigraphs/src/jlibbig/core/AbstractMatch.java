package jlibbig.core;

abstract public class AbstractMatch<A extends AbstractBigraph>  implements Match<A>{

	protected final A context;
	protected final A redex;
	protected final A param;
	
	/**
	 * @param context
	 * @param redex
	 * @param param
	 */
	protected AbstractMatch(A context, A redex, A param){
		this.context = context;
		this.redex = redex;
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
