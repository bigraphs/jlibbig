package it.uniud.mads.jlibbig.core;

abstract public class AbstractMatch<A extends Bigraph<?>> implements Match<A> {

	protected A context;
	protected A redex;
	protected A param;

	/**
	 * @param context
	 * @param redex
	 * @param param
	 */
	protected AbstractMatch(A context, A redex, A param) {
		this.context = context;
		this.redex = redex;
		this.param = param;
	}

	/**
	 * @see it.uniud.mads.jlibbig.core.Match#getContext()
	 */
	@Override
	public A getContext() {
		return this.context;
	}

	/**
	 * @see it.uniud.mads.jlibbig.core.Match#getRedex()
	 */
	@Override
	public A getRedex() {
		return this.redex;
	}

	/**
	 * @see it.uniud.mads.jlibbig.core.Match#getParam()
	 */
	@Override
	public A getParam() {
		return this.param;
	}

}
