package jlibbig;

import java.util.*;

public class SignatureBuilder<C extends GraphControl> {

	private Map<String, C> ctrls = new HashMap<>();
	
	public SignatureBuilder(){}
	
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

	public boolean contains(String name){
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
