package jlibbig.udlang;

import jlibbig.core.std.Bigraph;
import jlibbig.core.std.BigraphRewritingRule;
import jlibbig.core.std.Signature;

import java.util.*;

/**
 * This class stores a set of bigraphs and a set of reactions. Bigraphs, redexes
 * and reacta of the same system share the same signature and this signature
 * (system's signature) can't change.
 */
public class BigraphRewritingSystem {
	final private Signature signature;
	final private Set<Bigraph> bigraphs;
	final private Set<BigraphRewritingRule> reactions;

	/**
	 * @param sig
	 *            signature used for every bigraph and reaction of this system.
	 */
	public BigraphRewritingSystem(Signature sig) {
		if (sig == null)
			throw new IllegalArgumentException("Signature can't be null");
		signature = sig;
		bigraphs = new LinkedHashSet<>();
		reactions = new HashSet<>();
	}

	/**
	 * Get the system's signature
	 * 
	 * @return The system's signature
	 * @see Signature
	 */
	public Signature getSignature() {
		return signature;
	}

	/**
	 * Get the set of system's bigraphs.
	 * 
	 * @return The set of bigraphs stored in the system.
	 * @see Bigraph
	 */
	public Set<Bigraph> getBigraphs() {
		return Collections.unmodifiableSet(bigraphs);
	}

	/**
	 * Get the set of reactions.
	 * 
	 * @return A set of Reaction.
	 * @see Bigraph
	 * @see BigraphRewritingRule
	 */
	public Set<BigraphRewritingRule> getReactions() {
		return Collections.unmodifiableSet(reactions);
	}

	/**
	 * Add a bigraph to the system. Its signature must match with the system's
	 * signature.
	 * 
	 * @param b
	 *            the bigraph that will be added to the system.
	 * @throws RuntimeException
	 *             if both signatures don't match.
	 * @see Bigraph
	 */
	public void addBigraph(Bigraph b) {
		if (signature != b.getSignature())
			throw new RuntimeException(
					"Can't add a Bigraph to a BigraphSystem. Its Signature must be equal to the BigraphSystem's signature");
		bigraphs.add(b);
	}

	/**
	 * Add a reaction to the system. Signatures of redex and reactum must match
	 * with the system's signature.
	 * 
	 * @param redex
	 *            the bigraph representing reaction's redex
	 * @param reactum
	 *            the bigraph representing reaction's reactum
	 * @throws RuntimeException
	 *             if signatures don't match
	 * @see Bigraph
	 */
	public void addReaction(Bigraph redex, Bigraph reactum,
			int... instantiationMap) {
		if (signature != redex.getSignature()
				|| signature != reactum.getSignature())
			throw new RuntimeException(
					"Can't add a Reaction to a BigraphSystem. Both ( redex and reactum ) Signatures must be equal to the BigraphSystem's signature");
		if (redex.getRoots().size() != reactum.getRoots().size())
			throw new RuntimeException(
					"The number of roots in redex and reactum must be the same");
		if (!redex.getOuterNames().containsAll(reactum.getOuterNames())
				|| !reactum.getOuterNames().containsAll(redex.getOuterNames()))
			throw new RuntimeException(
					"Redex and reactum must have the same set of outernames.");
		if (!redex.getInnerNames().containsAll(reactum.getInnerNames())
				|| !reactum.getInnerNames().containsAll(redex.getInnerNames()))
			throw new RuntimeException(
					"Redex and reactum must have the same set of innernames.");
		reactions
				.add(new BigraphRewritingRule(redex, reactum, instantiationMap));
	}

	/**
	 * Return a view of the system.
	 * 
	 * @return a String representing the system.
	 * @see Bigraph#toString()
	 */
	@Override
	public String toString() {
		String nl = System.getProperty("line.separator");
		StringBuilder str = new StringBuilder();

		str.append("REACTIONS:").append(nl);
		for (BigraphRewritingRule reac : reactions) {
			str.append(reac.getRedex().toString()).append(nl).append("-->")
					.append(nl).append(reac.getReactum().toString()).append(nl)
					.append(nl);
		}

		str.append("BIGRAPHS:").append(nl);
		for (Bigraph b : bigraphs) {
			str.append(b.toString()).append(nl).append(nl);
		}

		return str.toString();
	}
}
