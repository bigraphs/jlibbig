/**
 * 
 */
package jlibbig.core;

import java.util.*;

import jlibbig.core.exceptions.IncompatibleSignatureException;
import jlibbig.core.exceptions.InvalidInstantiationRuleException;

public class AgentRewritingRule implements RewritingRule<Bigraph>{

	final private boolean[] neededParams;
	final private boolean[] cloneParams; 
	
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
		this.eta = new BigraphInstantiationMap(eta);
		
		if (reactum.getSignature() != redex.getSignature()) {
			throw new IncompatibleSignatureException(reactum.getSignature(), redex.getSignature(),
					"Redex and reactum should have the same singature.");
		}
		if (redex.getSites().size() != this.eta.getPlaceCodomain()) {
			throw new InvalidInstantiationRuleException("The instantiation rule does not match the redex inner interface.");
		}
		if (reactum.getSites().size() != this.eta.getPlaceDomain()) {
			throw new InvalidInstantiationRuleException("The instantiation rule does not match the reactum inner interface.");
		}
		
		
		this.neededParams = new boolean[this.eta.getPlaceCodomain()];
		this.cloneParams = new boolean[this.eta.getPlaceDomain()];
		int prms[] = new int[this.eta.getPlaceDomain()];
		for(int i = 0;i< this.eta.getPlaceDomain();i++){
			int j = this.eta.getPlaceInstance(i);
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
				throw new IncompatibleSignatureException(agent.signature, redex.signature,
					"Agent and redex should have the same singature.");
		}
		return new RewriteIterable(agent);
	}
	
	private class RewriteIterable implements Iterable<Bigraph> {

		private final Bigraph target;

		private Iterable<Match<Bigraph>> mAble;

		RewriteIterable(Bigraph target) {
			this.target = target;
		}

		@Override
		public Iterator<Bigraph> iterator() {
			if (mAble == null)
				mAble = AgentMatcher.DEFAULT.match(target, redex,neededParams);
			return new RewriteIterator();
		}

		private class RewriteIterator implements Iterator<Bigraph> {

			Iterator<Match<Bigraph>> matches;

			@Override
			public boolean hasNext() {
				if (matches == null)
					matches = mAble.iterator();
				return matches.hasNext();
			}

			@Override
			public Bigraph next() {
				if (hasNext()) {
					AgentMatch match = (AgentMatch) matches
							.next();
					BigraphBuilder bb = new BigraphBuilder(redex.getSignature());
					for(int i = eta.getPlaceDomain()-1;0 <= i; i--){
						bb.leftParallelProduct(
								match.params.get(eta.getPlaceInstance(i)), !cloneParams[i]);
					}
					bb.outerCompose(match.redex,true);
					bb.outerCompose(match.context,true);
					return bb.makeBigraph();
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
