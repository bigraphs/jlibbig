/**
 * 
 */
package jlibbig.core;

import java.util.*;

public class BigraphAgentRewriting implements ReactionRule<Bigraph> {

	final private boolean[] neededParams;
	final private boolean[] cloneParams; 
	
	final Bigraph redex;
	final Bigraph reactum;
	final InstantiationMap eta;
	
	/**
	 * @param redex
	 * @param reactum
	 * @param eta
	 */
	public BigraphAgentRewriting(Bigraph redex, Bigraph reactum, int... eta) {
		this(redex,reactum,new InstantiationMap(eta));
	}
	
	/**
	 * @param redex
	 * @param reactum
	 * @param eta
	 */
	public BigraphAgentRewriting(Bigraph redex, Bigraph reactum,
			InstantiationMap eta) {
		if (reactum.getSignature() != redex.getSignature()) {
			throw new IncompatibleSignatureException(reactum.getSignature(), redex.getSignature(),
					"Redex and reactum should have the same singature.");
		}
		if (redex.getSites().size() != eta.getPlaceCodomain()) {
			throw new InvalidInstantiationRule("The instantiation rule does not match the redex inner interface.");
		}
		if (reactum.getSites().size() != eta.getPlaceDomain()) {
			throw new InvalidInstantiationRule("The instantiation rule does not match the reactum inner interface.");
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

	public static class InstantiationMap{ // implements InstantiationRule<Bigraph>{

		private int map[];
		private int dom;
		private int cod;
		
		public InstantiationMap(int... map) {
			dom = map.length;
			cod = 0;
			this.map = new int[dom];
			for(int i = 0;i< map.length;i++){
				if(map[i] < 0){
					throw new IllegalArgumentException("Invalid image");
				}else if(map[i] > cod){
					cod = map[i];
				}
				this.map[i] = map[i];
			}
		}

		public int getPlaceDomain() {
			return dom;
		}

		public int getPlaceCodomain() {
			return cod;
		}

		public int getPlaceInstance(int arg) {
			if(arg > 0 && arg < dom){
				return map[arg];
			}else{
				return -1;
			}
		}
	}
	
	@Override
	public Iterable<Bigraph> apply(Bigraph to) {
		if (!to.isGround()) {
			throw new UnsupportedOperationException(
					"Agent should be a bigraph with empty inner interface i.e. ground.");
		}
		if (to.signature != redex.signature) {
				throw new IncompatibleSignatureException(to.signature, redex.signature,
					"Agent and redex should have the same singature.");
		}
		return new RewriteIterable(to);
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
				mAble = BigraphAgentMatcher.DEFAULT.match(target, redex,neededParams);
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
					BigraphAgentMatch match = (BigraphAgentMatch) matches
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
