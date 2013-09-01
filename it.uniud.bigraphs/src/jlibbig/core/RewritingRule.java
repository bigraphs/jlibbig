package jlibbig.core;

/**
 * Interface for bigraph's reactions.
 * @param <B>
 * 			Bigraph's type used to represent a reaction.
 */
public interface RewritingRule<B extends AbstractBigraph> extends ReactionRule<B>{
	/**
	 * Gets the redex of the rewriting rule.
	 * @return a bigraph of type <code>B</code> describing the rule redex.
	 */
	public B getRedex();
	
	/**
	 * Gets the reactum of the rewriting rule.
	 * @return a bigraph of type <code>B</code> describing the rule reactum.
	 */
	public B getReactum();
	
	
	/**
	 * Get the instantiation map of the rewriting rule.
	 * @return an instantiation rule for bigraphs of type <code>B</code> describing the rule instantiation map 
	 */
	public InstantiationRule<? extends B> getInstantiationRule();
}
