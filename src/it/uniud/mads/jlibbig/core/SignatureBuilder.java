package it.uniud.mads.jlibbig.core;

import java.util.*;

/**
 * The class provides methods for signature construction since {@link Signature}
 * is immutable. Every instance of the class maintains a collection of instances
 * of {@link Control} which are used to instantiate signatures on demand.
 */
public class SignatureBuilder<C extends Control> {

	private Map<String, C> ctrls = new HashMap<>();

	/**
	 * Creates a signature with the controls in the builder and a fresh
	 * signature identifier.
	 * 
	 * @return a signature.
	 */
	public Signature<C> makeSignature() {
		return new Signature<>(ctrls.values());
	}

	/**
	 * Creates a signature with the controls in the builder and the given
	 * signature identifier.
	 * 
	 * @return a signature.
	 */
	public Signature<C> makeSignature(String usid) {
		return new Signature<>(usid, ctrls.values());
	}

	/**
	 * Adds a control to the builder. Controls with the same name are substituted.
	 * @param control the control to be added.
	 */
	public void add(C control) {
		ctrls.put(control.getName(), control);
	}

	/**
	 * Checks if a name is already used by a signature's control
	 * 
	 * @param name
	 *            the name of the control to be looked for.
	 * @return {@literal true} if a control with the given name is present in
	 *         the builder.
	 */
	public boolean contains(String name) {
		return ctrls.containsKey(name);
	}

	/**
	 * Gets the control corresponding to the given name.
	 * 
	 * @param name
	 *            the name of the control to be looked for.
	 * @return the corresponding control or {@literal null} if there is no
	 *         control for the given name.
	 */
	public C get(String name) {
		return ctrls.get(name);
	}

	/**
	 * Gets the collection of all controls in the builder.
	 * 
	 * @return the control collection.
	 */
	public Collection<C> getAll() {
		return Collections.unmodifiableCollection(ctrls.values());
	}

	/**
	 * Removes the control for the given name from the builder.
	 * 
	 * @param name
	 *            the name of the control to be removed.
	 */
	public void remove(String name) {
		ctrls.remove(name);
	}

	/**
	 * Removes all controls from the builder.
	 */
	public void clear() {
		ctrls.clear();
	}
}
