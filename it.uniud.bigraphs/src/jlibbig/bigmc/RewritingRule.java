package jlibbig.bigmc;

import jlibbig.core.*;
import jlibbig.core.abstractions.InstantiationRule;
import jlibbig.core.exceptions.IncompatibleInterfacesException;
import jlibbig.core.exceptions.IncompatibleSignatureException;
import jlibbig.core.exceptions.InvalidInstantiationRuleException;

public class RewritingRule implements
		jlibbig.core.abstractions.RewritingRule<AgentBigraph, ReactionBigraph> {
	final private boolean[] neededParams;
	final private boolean[] cloneParams;

	final ReactionBigraph redex;
	final ReactionBigraph reactum;
	final InstantiationRule<AgentBigraph> eta;

	public RewritingRule(ReactionBigraph redex, ReactionBigraph reactum) {
		if (!redex.isRedex())
			throw new IllegalArgumentException(
					"The first ReactionBigraph can't be used as a redex: two sites with the same index.");
		if (reactum.getSignature() != redex.getSignature()) {
			throw new IncompatibleSignatureException(reactum.getSignature(),
					redex.getSignature(),
					"Redex and reactum should have the same singature.");
		}

		if (redex.getRoots().size() != reactum.getRoots().size())
			throw new IncompatibleInterfacesException(redex, reactum,
					"Redex and Reactum must have the same number of roots.");

		int[] re_eta = new int[reactum.getSitesIndices().size()];

		for (int i = 0; i < reactum.getSitesIndices().size(); ++i) {
			if ((re_eta[i] = redex.getSitesIndices().indexOf(reactum.sites[i])) == -1)
				throw new RuntimeException("No site indexed as $"
						+ reactum.sites[i] + " in the redex.");
		}

		InstantiationMap eta = new InstantiationMap(redex.getSites().size(),
				re_eta);

		if (redex.getSites().size() != eta.getPlaceCodomain()) {
			throw new InvalidInstantiationRuleException(
					"The instantiation rule does not match the redex inner interface.");
		}
		if (reactum.getSites().size() != eta.getPlaceDomain()) {
			throw new InvalidInstantiationRuleException(
					"The instantiation rule does not match the reactum inner interface.");
		}
		this.redex = redex;
		this.reactum = reactum;
		this.eta = eta;

		this.neededParams = new boolean[eta.getPlaceCodomain()];
		this.cloneParams = new boolean[eta.getPlaceDomain()];
		int prms[] = new int[eta.getPlaceDomain()];
		for (int i = 0; i < eta.getPlaceDomain(); i++) {
			int j = eta.getPlaceInstance(i);
			neededParams[j] = true;
			prms[i] = j;
		}
		for (int i = 0; i < prms.length; i++) {
			if (cloneParams[i])
				continue;
			for (int j = i + 1; j < prms.length; j++) {
				cloneParams[j] = cloneParams[j] || (prms[i] == prms[j]);
			}
		}
	}

	public RewritingRule(Bigraph redex, Bigraph reactum) {
		this(new ReactionBigraph(redex), new ReactionBigraph(reactum));
	}

	public RewritingRule(Bigraph redex, Bigraph reactum, int... eta) {
		this(new ReactionBigraph(redex), new ReactionBigraph(reactum, eta));
	}

	public Signature getSignature() {
		return redex.getSignature();
	}

	@Override
	public ReactionBigraph getRedex() {
		return this.redex;
	}

	@Override
	public ReactionBigraph getReactum() {
		return this.reactum;
	}

	@Override
	public InstantiationRule<AgentBigraph> getInstantiationRule() {
		return this.eta;
	}

	@Override
	public Iterable<AgentBigraph> apply(AgentBigraph to) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented yet.");
	}

}
