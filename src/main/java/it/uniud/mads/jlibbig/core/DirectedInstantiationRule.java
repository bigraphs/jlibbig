package it.uniud.mads.jlibbig.core;

/**
 * 
 * @param <B> the kind of bigraphs the rule can be applied to.
 * 
 * @see DirectedRewritingRule
 */
public interface DirectedInstantiationRule<B extends DirectedBigraph<?>> {
	Iterable<? extends B> instantiate(B parameters);
}
