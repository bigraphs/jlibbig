/**
 * 
 */
package it.uniud.mads.jlibbig.core.std;

import java.util.*;

import it.uniud.mads.jlibbig.core.exceptions.*;

public class AgentRewritingRule extends BigraphRewritingRule {

	private final static boolean DEBUG = true;
	private final static boolean DEBUG_PRINT_MATCH = DEBUG;
	private final static boolean DEBUG_PRINT_RESULT = DEBUG;

	final private boolean[] neededParam;
	final private boolean[] cloneParam;

	private AgentMatcher matcher;

	public AgentRewritingRule(Bigraph redex, Bigraph reactum, int... eta) {
		this(AgentMatcher.DEFAULT, redex, reactum, new BigraphInstantiationMap(
				redex.sites.size(), eta));
	}

	public AgentRewritingRule(AgentMatcher matcher, Bigraph redex,
			Bigraph reactum, int... eta) {
		this(matcher, redex, reactum, new BigraphInstantiationMap(
				redex.sites.size(), eta));
	}

	public AgentRewritingRule(Bigraph redex, Bigraph reactum,
			BigraphInstantiationMap eta) {
		this(AgentMatcher.DEFAULT, redex, reactum, eta);
	}

	public AgentRewritingRule(AgentMatcher matcher, Bigraph redex,
			Bigraph reactum, BigraphInstantiationMap eta) {
		super(redex, reactum, eta);

		this.matcher = (matcher == null) ? AgentMatcher.DEFAULT : matcher;

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
			throw new IncompatibleSignatureException(
					"Agent and redex should have the same singature.",
					agent.getSignature(), redex.getSignature());
		}
		return new RewriteIterable(agent);
	}

	private class RewriteIterable implements Iterable<Bigraph> {

		private final Bigraph target;

		private Iterable<? extends AgentMatch> mAble;

		RewriteIterable(Bigraph target) {
			this.target = target;
		}

		@Override
		public Iterator<Bigraph> iterator() {
			if (mAble == null)
				mAble = matcher.match(target, redex, neededParam);
			return new RewriteIterator();
		}

		private class RewriteIterator implements Iterator<Bigraph> {

			Iterator<? extends AgentMatch> mTor;

			@Override
			public boolean hasNext() {
				if (mTor == null)
					mTor = mAble.iterator();
				return mTor.hasNext();
			}

			@Override
			public Bigraph next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}
				AgentMatch match = mTor.next();

				if (DEBUG_PRINT_MATCH)
					System.out.println(match);

				BigraphBuilder bb = new BigraphBuilder(redex.getSignature());
				for (int i = eta.getPlaceDomain() - 1; 0 <= i; i--) {
					bb.leftJuxtapose(match.params.get(eta.getPlaceInstance(i)),
							!cloneParam[i]);
				}
				Bigraph lambda = match.getParamWiring();
				for (EditableInnerName n : lambda.inners.values()) {
					if (!bb.containsOuterName(n.getName())) {
						lambda.inners.remove(n.getName());
						n.setHandle(null);
					}
				}
				for (int i = eta.getPlaceCodomain() - eta.getPlaceDomain(); i > 0; i--) {
					lambda.roots.remove(0);
					lambda.sites.remove(0);
				}
				for (int i = eta.getPlaceDomain() - eta.getPlaceCodomain(); i > 0; i--) {
					EditableRoot r = new EditableRoot();
					r.setOwner(lambda);
					EditableSite s = new EditableSite(r);
					lambda.roots.add(r);
					lambda.sites.add(s);
				}
				bb.outerCompose(lambda, true);
				Bigraph inreact = instantiateReactum(match);
				inreact = Bigraph.juxtapose(inreact, match.getRedexId());
				bb.outerCompose(inreact, true);
				bb.outerCompose(match.getContext(), true);
				Bigraph result = bb.makeBigraph(true);
				if (DEBUG_PRINT_RESULT)
					System.out.println(result);
				return result;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("");
			}

		}
	}
}
