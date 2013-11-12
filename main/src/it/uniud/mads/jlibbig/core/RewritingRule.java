package it.uniud.mads.jlibbig.core;

/**
 * A rewriting rule is a reaction rule described by means of rewrites i.e. the
 * reaction is a substitution of an occurrence (in the bigraph to which the rule
 * is applied) of the rule's redex with the rule's reactum. Redex and reactum of
 * a rewriting rule are described by means of two bigraphs ({@link #getRedex()}
 * and {@link #getReactum()} ). Occurrences are described by matches (cf.
 * {@link Match}) i.e. triples like <C,R,P> where R is the redex occurrence and
 * the bigraph P is the parameter. Then R is replaced by the reactum R' and P is
 * instantiated to P' in order to match R' inner interface. Parameter
 * instantiation is handled by instantiation rule returned by
 * {@link #getInstantiationRule()}.
 * 
 * 
 * @param <A>
 *            The kind of bigraphs the rules applies to.
 * @param <B>
 *            The kind of bigraphs used for describing redex and reactum.
 *            
 * @see RewritingSystem
 */
public interface RewritingRule<A extends Bigraph<?>, B extends Bigraph<?>>
		extends ReactionRule<A> {
	/**
	 * Gets the redex of the rewriting rule.
	 * 
	 * @return a bigraph describing the rule redex.
	 */
	public B getRedex();

	/**
	 * Gets the reactum of the rewriting rule.
	 * 
	 * @return a bigraph describing the rule reactum.
	 */
	public B getReactum();

	/**
	 * Get the instantiation map of the rewriting rule.
	 * 
	 * @return an instantiation rule to handle parameters of kind <code>A</code>.
	 */
	public InstantiationRule<? extends A> getInstantiationRule();
}
