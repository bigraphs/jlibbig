package jlibbig.core;

import java.util.Iterator;
import jlibbig.core.exceptions.*;

public class BigraphRewritingRule implements RewritingRule<Bigraph> {
	final Bigraph redex;
	final Bigraph reactum;
	final BigraphInstantiationMap eta;
	
	public BigraphRewritingRule(Bigraph redex, Bigraph reactum, int... eta) {
		this(redex,reactum,new BigraphInstantiationMap(redex.sites.size(),eta));
	}
	
	/**
	 * @param redex
	 * @param reactum
	 * @param eta
	 */
	protected BigraphRewritingRule(Bigraph redex, Bigraph reactum,
			BigraphInstantiationMap eta) {
		if (reactum.getSignature() != redex.getSignature()) {
			throw new IncompatibleSignatureException(reactum.getSignature(), redex.getSignature(),
					"Redex and reactum should have the same singature.");
		}
		if (redex.sites.size() < eta.getPlaceCodomain()) {
			throw new InvalidInstantiationRuleException("The instantiation rule does not match the redex inner interface.");
		}
		if (reactum.sites.size() != eta.getPlaceDomain()) {
			throw new InvalidInstantiationRuleException("The instantiation rule does not match the reactum inner interface.");
		}
		if (reactum.sites.size() != this.eta.getPlaceDomain()) {
			throw new InvalidInstantiationRuleException(
					"The instantiation rule does not match the reactum inner interface.");
		}
		if (redex.roots.size() != reactum.roots.size() || !redex.outers.containsAll(reactum.outers) || !reactum.outers.containsAll(redex.outers)){
			throw new IncompatibleInterfacesException(redex,reactum,
					"Redex and reactum should have the same outer interface.");
		}
		if (!redex.inners.containsAll(reactum.inners) || !reactum.inners.containsAll(redex.inners)){
			throw new IncompatibleInterfacesException(redex,reactum,
					"Redex and reactum should have the same outer interface.");
		}
		this.redex = redex;
		this.reactum = reactum;
		this.eta = eta;
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
	public InstantiationRule<Bigraph> getInstantiationRule() {
		return this.eta;
	}

	@Override
	public Iterable<Bigraph> apply(Bigraph to) {
		return new RewriteIterable(to);
	}
	
	private class RewriteIterable implements Iterable<Bigraph> {

		private final Bigraph target;

		private Iterable<? extends BigraphMatch> mAble;

		RewriteIterable(Bigraph target) {
			this.target = target;
		}

		@Override
		public Iterator<Bigraph> iterator() {
			if (mAble == null)
				mAble = BigraphMatcher.DEFAULT.match(target, redex);
			return new RewriteIterator();
		}

		private class RewriteIterator implements Iterator<Bigraph> {

			Iterator<? extends BigraphMatch> matches = null;
			Iterator<Bigraph> args = null;
			Bigraph big = null;

			@Override
			public boolean hasNext() {
				if (matches == null)
					matches = mAble.iterator();
				return matches.hasNext() || ((args != null) && args.hasNext());
			}

			@Override
			public Bigraph next() {
				if (hasNext()) {
					if(args == null){
						BigraphMatch match = matches.next();
						big = Bigraph.compose(match.getContext(), match.getRedex(), true);
					    args = eta.instantiate(match.getParam()).iterator();
					}
					if(args.hasNext()){
						Bigraph params = args.next();
						if(args.hasNext())
							return Bigraph.compose(big.clone(), params,true);
						else
							return Bigraph.compose(big, params,true);
					}
				}
				return null;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("");
			}

		}
	}
}