package it.uniud.mads.jlibbig.core;

/**
 * A reaction rule describes how a state of a bigraphical reactive system 
 * (cf. {ReactiveSystem}) can
 * evolve i.e. it can transform the bigraphs representing these states.
 * 
 * @param <B>
 *            The kind of bigraphs the rules applies to.
 *            
 * @see DirectedRewritingRule
 */
public interface DirectedReactionRule<B extends DirectedBigraph<?>> {
	public Iterable<B> apply(B to);
}
