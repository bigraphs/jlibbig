package it.uniud.mads.jlibbig.core.util;

import java.util.*;

/**
 * A weak HashSet. An element stored in the WeakHashSet might be garbage
 * collected, if there is no strong reference to this element.
 */
public class WeakHashSet<V> implements Set<V> {

	protected static final long serialVersionUID = 7157522610134013359L;

	protected final WeakHashMap<V, Boolean> map = new WeakHashMap<>();

	public Set<V> toSet() {
		return map.keySet();
	}

	@Override
	public boolean add(V arg0) {
		return map.put(arg0, true) == null;
	}

	@Override
	public boolean addAll(Collection<? extends V> arg0) {
		boolean res = false;
		for (V item : arg0) {
			res |= add(item);
		}
		return res;
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public boolean contains(Object arg0) {
		return map.get(arg0) == Boolean.TRUE;
	}

	@Override
	public boolean containsAll(Collection<?> arg0) {
		return map.keySet().containsAll(arg0);
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public Iterator<V> iterator() {
		return map.keySet().iterator();
	}

	@Override
	public boolean remove(Object arg0) {
		return map.remove(arg0) == Boolean.TRUE;
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		boolean res = false;
		for (Object key : arg0) {
			res |= remove(key);
		}
		return res;
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		boolean res = false;
		for (V key : map.keySet()) {
			if (!arg0.contains(key)) {
				res |= remove(key);
			}
		}
		return res;
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public Object[] toArray() {
		return map.keySet().toArray();
	}

	@Override
	public <T> T[] toArray(T[] arg0) {
		return map.keySet().toArray(arg0);
	}
}
