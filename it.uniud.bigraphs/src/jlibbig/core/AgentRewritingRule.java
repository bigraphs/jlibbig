/**
 * 
 */
package jlibbig.core;

import java.util.*;

import jlibbig.core.exceptions.*;

public class AgentRewritingRule extends BigraphRewritingRule {

	final private boolean[] neededParam;
	final private boolean[] cloneParam;

	public AgentRewritingRule(Bigraph redex, Bigraph reactum, int... eta) {
		super(redex,reactum, new BigraphInstantiationMap(redex.sites.size(),eta));
		
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

	@Override
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
				mAble = AgentMatcher.DEFAULT.match(target, redex, null, neededParam);
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
					Bigraph lambda = match.getLinking();
					Set<? extends LinkFacet> ons = bb.getOuterNames();
					Iterator<EditableInnerName> ir = lambda.inners.iterator();
					while(ir.hasNext()){
						EditableInnerName i = ir.next();
						if(!ons.contains(i)){
							ir.remove();
						}
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
