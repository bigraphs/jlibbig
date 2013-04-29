package jlibbig;

import java.util.*;

public class BigraphRewritingRule {

	private final Bigraph _lhs;
	private final Bigraph _rhs;
	private final Map<Integer, Integer> _map = new HashMap<>();

	private final boolean _dup;
	
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
			//check signatures
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
	
	public Integer map(int site){
		return _map.get(site);
	}
	
	public Map<Integer,Integer> getInstantiationMap(){
		return Collections.unmodifiableMap(_map);
	}

	public BigraphView getRactumView() {
		return new BigraphView(_rhs);
	}

	public BigraphView getRedexView() {
		return new BigraphView(_lhs);
	}

	public Bigraph getRactum() {
		return _rhs.clone();
	}

	public Bigraph getRedex() {
		return _lhs.clone();
	}

	public Bigraph apply(BigraphMatch to) {
		Bigraph args = Bigraph.makeEmpty(_lhs.getSignature());
		int w = _rhs.getInnerFace().getWidth();
		for(int i = 0; i < w; i--){
			args.juxtapose(to.getArg(i));
		}
		return to.getContext().compose(to.getRedex()).compose(args);
	}

	public List<Bigraph> apply(Bigraph to) {
		throw new UnsupportedOperationException("Not implemented yet");
	}
}
