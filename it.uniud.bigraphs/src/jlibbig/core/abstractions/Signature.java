package jlibbig.core.abstractions;

import java.util.*;

/**
 * Bigraphs' signatures are immutable. To make a signature, users can use
 * {@link SignatureBuilder}.
 */
public class Signature<C extends Control> implements Iterable<C> {

	final private Map<String, C> ctrls = new HashMap<>();

	final protected String USID;

	public Signature(Collection<? extends C> controls) {
		this(null, controls);
	}

	public Signature(String usid, Collection<? extends C> controls) {
		for (C c : controls) {
			if (ctrls.put(c.getName(), c) != null) {
				throw new IllegalArgumentException(
						"Controls must be uniquely named within the same signature");
			}
		}
		this.USID = (usid == null || usid.trim().length() == 0) ? UUID
				.randomUUID().toString() : usid;
	}

	@SafeVarargs
	public Signature(C... controls) {
		this(null, controls);
	}

	@SafeVarargs
	public Signature(String usid, C... controls) {
		for (C c : controls) {
			if (ctrls.put(c.getName(), c) != null) {
				throw new IllegalArgumentException(
						"Controls must be uniquely named within the same signature");
			}
		}
		this.USID = (usid == null || usid.trim().length() == 0) ? UUID
				.randomUUID().toString() : usid;
	}

	protected void add(C control) {
		if (ctrls.containsKey(control.getName())
				&& !ctrls.containsValue(control)) {
			throw new IllegalArgumentException(
					"Controls must be uniquely named within the same signature");
		}
		ctrls.put(control.getName(), control);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + USID.hashCode();
		result = prime * result + ctrls.hashCode();
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Signature))
			return false;
		Signature<C> other = (Signature<C>) obj;
		if (!USID.equals(other.USID))
			return false;
		for (Control c : ctrls.values()) {
			if (!c.equals(other.getByName(c.getName())))
				return false;
		}
		if (!ctrls.equals(other.ctrls))
			return false;
		return true;
	}

	/**
	 * Get a control (if present), specifying its name.
	 * 
	 * @param name
	 *            name of the control
	 * @return the retrieved control
	 */
	public C getByName(String name) {
		return ctrls.get(name);
	}

	public String getUSID() {
		return this.USID.toString();
	}

	@Override
	public String toString() {
		return USID + ":" + ctrls.values();
	}

	public boolean contains(Control arg0) {
		// very naive
		return this.ctrls.containsValue(arg0);
	}

	public boolean containsAll(Collection<?> arg0) {
		// very naive
		return this.ctrls.values().containsAll(arg0);
	}

	public boolean isEmpty() {
		return this.ctrls.isEmpty();
	}

	@Override
	public Iterator<C> iterator() {
		return Collections.unmodifiableMap(this.ctrls).values().iterator();
	}

	public int size() {
		return this.ctrls.size();
	}
}
