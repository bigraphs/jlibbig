package it.uniud.mads.jlibbig.core.util;

import java.util.*;

/**
 * Bidirectional Map: two sets with a bijective function from one set to the
 * other.
 * 
 * @param <A>
 *            Type of the first set
 * @param <B>
 *            Type of the second set
 */
public class BidMap<A, B> implements Map<A, B> {

	private Map<A, B> _mapA = new HashMap<>();
	private Map<B, A> _mapB = new HashMap<>();

	public BidMap() {
	}

	public BidMap(Map<? extends A, ? extends B> map) {
		_mapA.putAll(map);
		for (A a : map.keySet()) {
			B b = map.get(a);
			if (_mapB.containsKey(b))
				throw new IllegalArgumentException(
						"The given map is not bidirectional");
			_mapB.put(b, a);
		}
	}

	private BidMap(Map<A, B> mapA, Map<B, A> mapB) {
		_mapA = mapA;
		_mapB = mapB;
	}

	/**
	 * Retrieve the bidirectional map from the second set to the first.
	 * 
	 * @return the inverse bidirectional map.
	 */
	public BidMap<B, A> getInverse() {
		return new BidMap<>(_mapB, _mapA);
	}

	@Override
	public void clear() {
		this._mapA.clear();
		this._mapB.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		return _mapA.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return _mapB.containsKey(value);
	}

	@Override
	public Set<java.util.Map.Entry<A, B>> entrySet() {
		return _mapA.entrySet();
	}

	@Override
	public B get(Object key) {
		return _mapA.get(key);
	}

	public A getKey(Object value) {
		return _mapB.get(value);
	}

	@Override
	public boolean isEmpty() {
		return _mapA.isEmpty();
	}

	@Override
	public Set<A> keySet() {
		return _mapA.keySet();
	}

	@Override
	public B put(A key, B value) {
		B b = _mapA.get(key);
		A a = _mapB.get(value);
		if (a != null && a != key)
			throw new IllegalArgumentException(
					"This insertion violates bidirectionality");
		_mapA.put(key, value);
		_mapB.put(value, key);
		return b;
	}

	@Override
	public void putAll(Map<? extends A, ? extends B> map) {
		Map<A, B> mapA = new HashMap<>();
		Map<B, A> mapB = new HashMap<>();
		mapA.putAll(map);
		for (A a : map.keySet()) {
			B b = map.get(a);
			if (mapB.containsKey(b))
				throw new IllegalArgumentException(
						"The given map is not bidirectional");
			mapB.put(b, a);
		}
		_mapA.putAll(mapA);
		_mapB.putAll(mapB);
	}

	@Override
	public B remove(Object key) {
		B b = _mapA.remove(key);
		_mapB.remove(b);
		return b;
	}

	@Override
	public int size() {
		return _mapA.size();
	}

	@Override
	public Collection<B> values() {
		return _mapB.keySet();
	}

}
