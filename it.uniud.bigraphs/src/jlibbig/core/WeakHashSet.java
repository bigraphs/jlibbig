package jlibbig.core;

import java.util.*;
import java.lang.ref.*;

/**
 * A weak HashSet. An element stored in the WeakHashSet might be garbage
 * collected, if there is no strong reference to this element.
 */
class WeakHashSet<V> implements Set<V> {
	protected static final long serialVersionUID = 7157522610134013359L;

	protected final ReferenceQueue<V> queue = new ReferenceQueue<>();

	protected final Set<WeakElement<V>> set = new HashSet<>();

	/**
	 * Removes all garbage collected values with their keys from the map. Since
	 * we don't know how much the ReferenceQueue.poll() operation costs, we
	 * should call it only in the add() method.
	 */
	private final void processQueue() {
		Reference<? extends V> ref = null;
		do {
			ref = this.queue.poll();
			set.remove(ref);
		} while (ref != null);
	}

	public Set<V> toSet() {
		Set<V> res = new HashSet<>();
		for (WeakElement<V> item : set) {
			V value = item.get();
			if (value == null)
				set.remove(item);
			else
				res.add(value);
		}
		return res;
	}

	@Override
	public boolean add(V item) {
		processQueue();
		return set.add(WeakElement.create(item, this.queue));
	}

	@Override
	public boolean addAll(Collection<? extends V> items) {
		processQueue();
		boolean res = false;
		for (V item : items) {
			res |= set.add(WeakElement.create(item, this.queue));
		}
		return res;
	}

	@Override
	public void clear() {
		set.clear();
		processQueue();
	}

	@Override
	public boolean contains(Object item) {
		return set.contains(WeakElement.create(item));
	}

	@Override
	public boolean containsAll(Collection<?> items) {
		boolean res = false;
		for (Object item : items) {
			res |= set.contains(WeakElement.create(item));
		}
		return res;
	}

	@Override
	public boolean isEmpty() {
		return set.isEmpty();
	}

	@Override
	public Iterator<V> iterator() {
		processQueue();
		// get an iterator of the superclass WeakHashSet
		final Iterator<WeakElement<V>> ir = set.iterator();
		return new Iterator<V>() {
			public boolean hasNext() {
				return ir.hasNext();
			}

			public V next() {
				WeakElement<V> ref = ir.next();
				return (ref == null) ? null : ref.get();
			}

			public void remove() {
				ir.remove();
			}
		};
	}

	@Override
	public boolean remove(Object item) {
		boolean res = set.remove(WeakElement.create(item));
		processQueue();
		return res;
	}

	@Override
	public boolean removeAll(Collection<?> items) {
		boolean res = false;
		for (Object item : items) {
			res |= set.remove(WeakElement.create(item));
		}
		processQueue();
		return res;
	}

	@Override
	public boolean retainAll(Collection<?> items) {
		boolean res = false;
		for (WeakElement<V> item : set) {
			V value = item.get();
			if (value == null)
				set.remove(item);
			else if (!items.contains(value)) {
				res |= set.remove(item);
			}
		}
		// processQueue(); redundant
		return res;
	}

	@Override
	public int size() {
		return set.size();
	}

	@Override
	public Object[] toArray() {
		return toSet().toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return toSet().toArray(a);
	}

	/**
	 * A WeakHashSet stores objects of class WeakElement. A WeakElement wraps
	 * the element that should be stored in the WeakHashSet. WeakElement
	 * inherits from java.lang.ref.WeakReference. It redefines equals and
	 * hashCode which delegate to the corresponding methods of the wrapped
	 * element.
	 */
	static protected class WeakElement<V> extends WeakReference<V> {
		private int hash; /*
						 * Hashcode of key, stored here since the key may be
						 * tossed by the GC
						 */

		WeakElement(V o) {
			super(o);
			hash = o.hashCode();
		}

		WeakElement(V o, ReferenceQueue<V> q) {
			super(o, q);
			hash = o.hashCode();
		}

		static <T> WeakElement<T> create(T o) {
			return (o == null) ? null : new WeakElement<T>(o);
		}

		static <T> WeakElement<T> create(T o, ReferenceQueue<T> q) {
			return (o == null) ? null : new WeakElement<T>(o, q);
		}

		/*
		 * A WeakElement is equal to another WeakElement iff they both refer to
		 * objects that are, in turn, equal according to their own equals
		 * methods
		 */
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (!(o instanceof WeakElement))
				return false;
			V t = this.get();
			if (t == null)
				return false;
			return t.equals(((WeakElement<?>) o).get());
		}

		public int hashCode() {
			return hash;
		}
	}
}
