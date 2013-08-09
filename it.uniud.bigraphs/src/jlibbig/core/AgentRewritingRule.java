/**
 * 
 */
package jlibbig.core;

import java.util.*;

import jlibbig.core.exceptions.IncompatibleSignatureException;
import jlibbig.core.exceptions.InvalidInstantiationRuleException;

public class AgentRewritingRule implements RewritingRule<Bigraph> {

	final private boolean[] neededParam;
	final private boolean[] cloneParam;

	final Bigraph redex;
	final Bigraph reactum;
	final BigraphInstantiationMap eta;

	/**
	 * @param redex
	 * @param reactum
	 * @param eta
	 */
	public AgentRewritingRule(Bigraph redex, Bigraph reactum, int... eta) {

		this.redex = redex;
		this.reactum = reactum;
		this.eta = new BigraphInstantiationMap(redex.sites.size(),eta);

		if (reactum.getSignature() != redex.getSignature()) {
			throw new IncompatibleSignatureException(reactum.getSignature(),
					redex.getSignature(),
					"Redex and reactum should have the same singature.");
		}
		if (redex.sites.size() < this.eta.getPlaceCodomain()) {
			throw new InvalidInstantiationRuleException(
					"The instantiation rule does not match the redex inner interface.");
		}
		if (reactum.sites.size() != this.eta.getPlaceDomain()) {
			throw new InvalidInstantiationRuleException(
					"The instantiation rule does not match the reactum inner interface.");
		}

		this.neededParam = new boolean[redex.sites.size()];
		this.cloneParam = new boolean[this.eta.getPlaceDomain()];
		int prms[] = new int[this.eta.getPlaceDomain()];
		for (int i = 0; i < this.eta.getPlaceDomain(); i++) {
			int j = this.eta.getPlaceInstance(i);
			neededParam[j] = true;
			prms[i] = j;
		}
		for (int i = 0; i < prms.length; i++) {
			if (cloneParam[i])
				continue;
			for (int j = i + 1; j < prms.length; j++) {
				cloneParam[j] = cloneParam[j] || (prms[i] == prms[j]);
			}
		}
	}

	@Override
	public Bigraph getRedex() {
		return this.redex;
	}

	@Override
	public Bigraph getReactum() {
		return this.reactum;
	}

	@Override
	public BigraphInstantiationMap getInstantiationRule() {
		return this.eta;
	}

	public Iterable<Bigraph> apply(Bigraph agent) {
		if (!agent.isGround()) {
			throw new UnsupportedOperationException(
					"Agent should be a bigraph with empty inner interface i.e. ground.");
		}
		if (!agent.signature.equals(redex.signature)) {
			throw new IncompatibleSignatureException(agent.signature,
					redex.signature,
					"Agent and redex should have the same singature.");
		}
		return new RewriteIterable(agent);
	}

	private class RewriteIterable implements Iterable<Bigraph> {

		private final Bigraph target;

		private Iterable<AgentMatch> mAble;

		RewriteIterable(Bigraph target) {
			this.target = target;
		}

		@Override
		public Iterator<Bigraph> iterator() {
			if (mAble == null)
				mAble = AgentMatcher.DEFAULT.match(target, redex, neededParam);
			return new RewriteIterator();
		}

		private class RewriteIterator implements Iterator<Bigraph> {

			Iterator<AgentMatch> matches;

			@Override
			public boolean hasNext() {
				if (matches == null)
					matches = mAble.iterator();
				return matches.hasNext();
			}

			@Override
			public Bigraph next() {
				if (hasNext()) {
					AgentMatch match = matches.next();
					BigraphBuilder bb = new BigraphBuilder(redex.getSignature());
					for (int i = eta.getPlaceDomain() - 1; 0 <= i; i--) {
						bb.leftParallelProduct(
								match.params.get(eta.getPlaceInstance(i)),
								!cloneParam[i]);
					}
					bb.outerCompose(match.redex, true);
					bb.outerCompose(match.context, true);
					return bb.makeBigraph(true);
				} else {
					return null;
				}
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("");
			}

		}
	}
}
