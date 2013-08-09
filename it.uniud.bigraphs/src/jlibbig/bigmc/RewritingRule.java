package jlibbig.bigmc;

import jlibbig.core.*;
import jlibbig.core.exceptions.IncompatibleSignatureException;
import jlibbig.core.exceptions.InvalidInstantiationRuleException;

public class RewritingRule implements jlibbig.core.RewritingRule<AbstBigraph> {
	final private boolean[] neededParams;
	final private boolean[] cloneParams; 
	
	final ReactionBigraph redex;
	final ReactionBigraph reactum;
	final BigraphInstantiationMap eta;
	
	public RewritingRule(ReactionBigraph redex, ReactionBigraph reactum, int... eta) {
		this(redex,reactum,new BigraphInstantiationMap(redex.getSites().size(),eta));
	}
	
	/**
	 * @param redex
	 * @param reactum
	 * @param eta
	 */
	public RewritingRule(ReactionBigraph redex, ReactionBigraph reactum,
			BigraphInstantiationMap eta) {
		if (reactum.getSignature() != redex.getSignature()) {
			throw new IncompatibleSignatureException(reactum.getSignature(), redex.getSignature(),
					"Redex and reactum should have the same singature.");
		}
		if (redex.getSites().size() != eta.getPlaceCodomain()) {
			throw new InvalidInstantiationRuleException("The instantiation rule does not match the redex inner interface.");
		}
		if (reactum.getSites().size() != eta.getPlaceDomain()) {
			throw new InvalidInstantiationRuleException("The instantiation rule does not match the reactum inner interface.");
		}
		this.redex = redex;
		this.reactum = reactum;
		this.eta = eta;
		
		
		this.neededParams = new boolean[eta.getPlaceCodomain()];
		this.cloneParams = new boolean[eta.getPlaceDomain()];
		int prms[] = new int[eta.getPlaceDomain()];
		for(int i = 0;i< eta.getPlaceDomain();i++){
			int j = eta.getPlaceInstance(i);
			neededParams[j] = true;
			prms[i] = j; 
		}
		for(int i = 0;i< prms.length; i++){
			if(cloneParams[i])
				continue;
			for(int j = i+1;j < prms.length;j++){
				cloneParams[j] = cloneParams[j] || (prms[i] == prms[j]);
			} 
		}
	}
	
	public Signature getSignature(){
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
	public InstantiationRule<Bigraph> getInstantiationRule() {
		return this.eta;
	}

	@Override
	public Iterable<AbstBigraph> apply(AbstBigraph to) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented yet.");
	}

}
