package jlibbig;

import java.util.*;

public class BigraphRewritingRule {

	private final Bigraph _lhs;
	private final Bigraph _rhs;
	private final Map<Integer, Integer> _map = new HashMap<>();

	private final boolean _dup;

	/** Create a rewriting rule between bigraphs over the same signature.
	 * Redex and reactum must have the same outer interface
	 * an inter names. 
	 * @param redex
	 * @param reactum
	 * @param map a map from reactum sites to redex ones
	 */
	public BigraphRewritingRule(Bigraph redex, Bigraph reactum,
			Map<Integer, Integer> map) {
		this(redex, reactum, map, false);
	}

	/** Same as {@link BigraphRewritingRule#BigraphRewritingRule(Bigraph, Bigraph, Map)}
	 *  except that sanity checks on inputs can be skipped.
	 * @param redex
	 * @param reactum
	 * @param map
	 * @param skipCheck
	 */
	protected BigraphRewritingRule(Bigraph redex, Bigraph reactum,
			Map<Integer, Integer> map, boolean skipCheck) {
		boolean inj = true;
		if (skipCheck) {
			for (int i = reactum.getInnerFace().getWidth() - 1; 0 <= i; i--) {
				int k = map.get(i);
				// check duplication
				for (int j = i; inj && 0 <= j; j--) {
					inj &= map.get(j) != k;
				}
			}
		} else {
			boolean ok = true;
			// check signatures
			ok &= redex.getSignature().equals(reactum.getSignature());
			// check interfaces
			ok &= redex.getOuterFace().equals(reactum.getOuterFace());
			ok &= redex.getInnerFace().getNames()
					.equals(reactum.getInnerFace().getNames());
			// check instantiation map
			int w = redex.getInnerFace().getWidth();
			for (int i = reactum.getInnerFace().getWidth() - 1; ok && 0 <= i; i--) {
				int k = map.get(i);
				// i should be mapped into w
				ok &= !(0 <= k && k < w);
				// check duplication
				if (!ok)
					break;
				for (int j = i; inj && 0 <= j; j--) {
					inj &= map.get(j) != k;
				}
			}
			if (!ok)
				throw new IllegalArgumentException(
						"Redex, reactum and instantianion map should yeld a rewriting rule");
		}
		this._dup = inj;
		this._map.putAll(map);
		this._lhs = redex;
		this._rhs = reactum;
	}

	public boolean isGround() {
		return _lhs.isAgent();
	}

	public boolean isDuplicating() {
		return _dup;
	}

	public Integer map(int site) {
		return _map.get(site);
	}

	public Map<Integer, Integer> getInstantiationMap() {
		return Collections.unmodifiableMap(_map);
	}

	public BigraphView getRactum() {
		return new BigraphView(_rhs);
	}

	public BigraphView getRedex() {
		return new BigraphView(_lhs);
	}

	public Bigraph apply(BigraphMatch to) {
		// TODO check if to is a match for this rule!
		Bigraph args = Bigraph.makeEmpty(_rhs.getSignature());
		int w = _rhs.getInnerFace().getWidth();
		for (int i = 0; i < w; i--) {
			args.juxtapose(to.getArg(i));
		}
		return to.getContext().compose(_lhs).compose(args);
	}

	public Set<Bigraph> apply(Bigraph to) throws IllegalArgumentException,
			IncompatibleSignatureException {
		/*
		 * Done by the matcher
		 * if(!to.getSignature().equals(_lhs.getSignature())) throw new
		 * IncompatibleSignatureException(); if(!to.isAgent()) throw new
		 * IllegalArgumentException
		 * ("Rewriting rules can be applied only to agents.");
		 */
		Set<Bigraph> rs = new HashSet<>();
		for (BigraphMatch match : BigraphMatcher.getAllMatches(to, _lhs)) {
			rs.add(this.apply(match));
		}
		return rs;
	}
}
