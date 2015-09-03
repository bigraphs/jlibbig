package it.uniud.mads.jlibbig.core.std;

import java.util.*;

/**
 * The class provides methods for signature construction since {@link Signature}
 * is immutable. Every instance of the class maintains a collection of instances
 * of {@link Control} which are used to instantiate signatures on demand.
 */
public class SignatureBuilder extends
		it.uniud.mads.jlibbig.core.SignatureBuilder<Control> {
	private Map<String, Control> ctrls = new HashMap<>();

	@Override
	public Signature makeSignature() {
		return new Signature(ctrls.values());
	}

	@Override
	public Signature makeSignature(String usid) {
		return new Signature(usid, ctrls.values());
	}

	/**
	 * Creates a new control and add it to the builder.
	 * 
	 * @param name
	 *            name of the control
	 * @param active
	 *            control's activity
	 * @param arity
	 *            number of ports
	 */
	public void add(String name, boolean active, int arity) {
		ctrls.put(name, new Control(name, active, arity));
	}

	@Override
	public void add(Control control) {
		ctrls.put(control.getName(), control);
	}

	@Override
	public boolean contains(String name) {
		return ctrls.containsKey(name);
	}

	@Override
	public Control get(String name) {
		return ctrls.get(name);
	}

	@Override
	public Collection<Control> getAll() {
		return Collections.unmodifiableCollection(ctrls.values());
	}

	@Override
	public void remove(String name) {
		ctrls.remove(name);
	}

	@Override
	public void clear() {
		ctrls.clear();
	}
}
