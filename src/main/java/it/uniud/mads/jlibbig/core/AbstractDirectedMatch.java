package it.uniud.mads.jlibbig.core;

/**
 * This class provides some support for implementing the interface {@link Match
 * <A>}. The support is limited to handling context, redex and parameter
 * composing a match; but no sanity checks are performed on these, nor on their
 * interfaces.
 * 
 * @param <A>
 *            the kind of bigraph the match belongs.
 */
abstract public class AbstractDirectedMatch<A extends DirectedBigraph<?>> 
		implements DirectedMatch<A> {

	protected A context;
	protected A redex;
	protected A param;

	/**
	 * Creates an object describing a match. There is no sanity check on the
	 * arguments and their interfaces.
	 * 
	 * @param context
	 * @param redex
	 * @param param
	 */
	protected AbstractDirectedMatch(A context, A redex, A param) {
		this.context = context;
		this.redex = redex;
		this.param = param;
	}

	@Override
	public A getContext() {
		return this.context;
	}

	@Override
	public A getRedex() {
		return this.redex;
	}

	@Override
	public A getParam() {
		return this.param;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Match:\ncontext = ").append(context)
				.append("\nredex = ").append(redex).append("\nparam = ")
				.append(param);
		return builder.toString();
	}

}
