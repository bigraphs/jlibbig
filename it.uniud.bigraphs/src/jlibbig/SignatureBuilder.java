package jlibbig;

import java.util.*;

/**
 * Helper for signature construction. Every instance maintains a set of controls
 * which are used to instantiate signatures on demand.
 * 
 * @see Signature
 * @param <C>
 *            the type of controls handled
 */
public class SignatureBuilder<C extends GraphControl> {

	private Map<String, C> ctrls = new HashMap<>();

	/**
	 * Creates an empty builder.
	 */
	public SignatureBuilder() {
	}

	/** 
	 * Creates a signature with the control present in the builder
	 * @return a signature
	 */
	public Signature<C> makeSignature() {
		return new Signature<C>(ctrls.values());
	}

	public void put(C control) {
		ctrls.put(control.getName(), control);
	}

	public void putAll(Collection<? extends C> controls) {
		for (C c : controls) {
			put(c);
		}
	}

	public boolean contains(String name) {
		return ctrls.containsKey(name);
	}

	public C get(String name) {
		return ctrls.get(name);
	}

	public Collection<C> getAll() {
		return Collections.unmodifiableCollection(ctrls.values());
	}

	public void remove(String name) {
		ctrls.remove(name);
	}

	public void clear() {
		ctrls.clear();
	}

}
