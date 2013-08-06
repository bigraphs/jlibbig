package jlibbig.core;

import java.util.*;

/**
 * Bigraphs' signatures are immutable. To make a signature, users can use {@link SignatureBuilder}.
 */
public class Signature implements Set<Control>{

	final private Map<String,Control> ctrls = new HashMap<>();

	final protected UUID USID;
	
	public Signature(Collection<Control > controls) {
		this(null, controls);
	}
	
	public Signature(UUID usid, Collection<Control > controls) {
		for(Control  c : controls){
			if(ctrls.put(c.getName(), c) != null){
				throw new IllegalArgumentException("Controls must be uniquely named within the same signature");
			}
		}
		this.USID = (usid == null) ? UUID.randomUUID() : usid;
	}
	
	public Signature(Control... controls) {
		this(null, controls);
	}
	
	public Signature(UUID usid, Control... controls) {
		for(Control  c : controls){
			if(ctrls.put(c.getName(), c) != null){
				throw new IllegalArgumentException("Controls must be uniquely named within the same signature");
			}
		}
		this.USID = (usid == null) ? UUID.randomUUID() : usid;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + USID.hashCode();
		result = prime * result + ctrls.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Signature))
			return false;
		Signature other = (Signature) obj;
		if (!USID.equals(other.USID))
			return false;
		for(Control c : ctrls.values()){
			if(!c.equals(other.getByName(c.getName())))
				return false;
		}
		for(String name : other.ctrls.keySet()){
			if(!ctrls.containsKey(name))
				return false;
		}
		if (!ctrls.equals(other.ctrls))
			return false;
		return true;
	}

	/**
	 * Get a control (if present), specifying its name.
	 * @param name name of the control
	 * @return the retrieved control
	 */
	public Control getByName(String name){
		return ctrls.get(name);
	}
		
	@Override
	public String toString() {
		return "Signature " + ctrls.values();
	}

	
	@Override
	public boolean add(Control  arg0) {
		throw new UnsupportedOperationException("Signatures are read-only sets");
	}

	@Override
	public boolean addAll(Collection<? extends Control > arg0) {
		throw new UnsupportedOperationException("Signatures are read-only sets");
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("Signatures are read-only sets");
	}

	@Override
	public boolean contains(Object arg0) {
		// very naive
		return this.ctrls.containsValue(arg0);
	}

	@Override
	public boolean containsAll(Collection<?> arg0) {
		// very naive
		return this.ctrls.values().containsAll(arg0);
	}

	@Override
	public boolean isEmpty() {
		return this.ctrls.isEmpty();
	}

	@Override
	public Iterator<Control> iterator() {
		return this.ctrls.values().iterator();
	}

	@Override
	public boolean remove(Object arg0) {
		throw new UnsupportedOperationException("Signatures are read-only sets");
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		throw new UnsupportedOperationException("Signatures are read-only sets");
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		throw new UnsupportedOperationException("Signatures are read-only sets");
	}

	@Override
	public int size() {
		return this.ctrls.size();
	}

	@Override
	public Object[] toArray() {
		return this.ctrls.values().toArray();
	}

	@Override
	public <T> T[] toArray(T[] arg0) {
		return this.ctrls.values().toArray(arg0);
	}

}
