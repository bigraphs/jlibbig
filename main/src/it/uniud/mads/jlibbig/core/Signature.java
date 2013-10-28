package it.uniud.mads.jlibbig.core;

import java.util.*;

/**
 * Objects created from this class are bigraphical signatures. A signature
 * defines the controls that can be assigned to the nodes of bigraphs over it. A
 * {@link Control} describes the arity the arity (i.e. the number of ports) of a
 * {@link Node} decorated with it. The class {@link SignatureBuilder} provides
 * some helper methods for signature construction.
 */
public class Signature<C extends Control> implements Iterable<C> {

	/**
	 * The map of the controls within the signature. Controls are indexed by
	 * their name.
	 */
	final protected Map<String, C> ctrls = new HashMap<>();

	/**
	 * The unique signature identifier.
	 */
	final protected String USID;

	/**
	 * Creates a new signature for the given list of controls; a fresh
	 * identifier is choosen. Controls can not have the same name.
	 * 
	 * @param controls
	 *            the controls contained within the signature.
	 */
	public Signature(Iterable<? extends C> controls) {
		this(null, controls);
	}

	/**
	 * Creates a new signature for the given identifier and list of controls.
	 * Controls can not have the same name.
	 * 
	 * @param usid
	 *            the identifier of the signature.
	 * @param controls
	 *            the controls contained within the signature.
	 */
	public Signature(String usid, Iterable<? extends C> controls) {
		for (C c : controls) {
			if (ctrls.put(c.getName(), c) != null) {
				throw new IllegalArgumentException(
						"Controls must be uniquely named within the same signature");
			}
		}
		this.USID = (usid == null || usid.trim().length() == 0) ? UUID
				.randomUUID().toString() : usid;
	}

	/**
	 * Creates a new signature for the given list of controls; a fresh
	 * identifier is choosen. Controls can not have the same name.
	 * 
	 * @param controls
	 *            the controls contained within the signature.
	 */
	@SafeVarargs
	public Signature(C... controls) {
		this(null, controls);
	}

	/**
	 * Creates a new signature for the given identifier and list of controls.
	 * Controls can not have the same name.
	 * 
	 * @param usid
	 *            the identifier of the signature.
	 * @param controls
	 *            the controls contained within the signature.
	 */
	@SafeVarargs
	public Signature(String usid, C... controls) {
		this(usid, Arrays.asList(controls));
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
	 * Checks if a given signature is equal to this one. Two signatures are
	 * equals when they contain the same controls (in the sense of their equals
	 * method) and have the same identifier.
	 * 
	 * @param other
	 *            the other signature.
	 * @return a boolean indicating whether the given signature is equal to this
	 *         one.
	 */
	public boolean equals(Signature<C> other) {
		return this.equals(other, false);
	}

	/**
	 * Checks if a given signature is equal to this one; but optionally ignores
	 * the signature identifiers. Two signatures are equals when they contain
	 * the same controls (in the sense of their equals method) and have the same
	 * identifier.
	 * 
	 * @param other
	 *            the other signature.
	 * @param ignoreUSID
	 *            if {@true} identifiers are ignored.
	 * @return a boolean indicating whether the given signature is equal to this
	 *         one.
	 */
	public boolean equals(Signature<C> other, boolean ignoreUSID) {
		if (this == other)
			return true;
		if (other == null)
			return false;
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
	 * Gets the identifier of the signature.
	 * 
	 * @return the signature identifier.
	 */
	public String getUSID() {
		return this.USID.toString();
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

	@Override
	public String toString() {
		return USID + ":" + ctrls.values();
	}

	/**
	 * Checks whether there is a control for the given name.
	 * 
	 * @param name
	 *            the name of the control to be looked for.
	 * @return a boolean indicating whether there is such a control.
	 */
	public boolean contains(String name) {
		return this.ctrls.containsKey(name);
	}

	/**
	 * Checks whether the given control belong to this signature.
	 * 
	 * @param control
	 *            the control to be looked for.
	 * @return a boolean indicating whether the control is in the signature.
	 */
	public boolean contains(Control control) {
		return this.ctrls.containsValue(control);
	}

	/**
	 * @return a boolean indicating whether the signature is empty.
	 */
	public boolean isEmpty() {
		return this.ctrls.isEmpty();
	}

	@Override
	public Iterator<C> iterator() {
		return Collections.unmodifiableMap(this.ctrls).values().iterator();
	}

	/**
	 * @return the cardinality of the signature.
	 */
	public int size() {
		return this.ctrls.size();
	}
}
