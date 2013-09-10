package jlibbig.core.abstractions;

import java.util.*;

/**
 * Helper for signature construction. Every instance maintains a set of controls
 * which are used to instantiate signatures on demand.
 * 
 * @see Signature
 */
public class SignatureBuilder<C extends Control> {

	private Map<String, C> ctrls = new HashMap<>();

	/**
	 * Creates a signature with the control present in the builder
	 * @return a signature
	 */
	public Signature<C> makeSignature() {
		return new Signature<C>(ctrls.values());
	}

	/**
	 * Creates a signature with the control present in the builder and the given
	 * signature identifier.
	 * @return a signature
	 */
	public Signature<C> makeSignature(String usid) {
		return new Signature<C>(usid, ctrls.values());
	}

	public void put(C control) {
		ctrls.put(control.getName(), control);
	}
	
	/**
	 * Check if a name is already used by a signature's control
	 * @param name control's name that will be checked
	 * @return boolean value
	 */
	public boolean contains(String name) {
		return ctrls.containsKey(name);
	}

	/**
	 * Get the control corresponding to the name in input
	 * @param name control's name
	 * @return the corresponding control
	 */
	public C  get(String name) {
		return ctrls.get(name);
	}

	/**
	 * Get a collection of all controls in the signature
	 * @return collection of signature's controls
	 */
	public Collection<C> getAll() {
		return Collections.unmodifiableCollection(ctrls.values());
	}

	/**
	 * Remove a control from the signature.
	 * @param name control's name
	 */
	public void remove(String name) {
		ctrls.remove(name);
	}

	/**
	 * Remove all controls from the signature.
	 */
	public void clear() {
		ctrls.clear();
	}
}
