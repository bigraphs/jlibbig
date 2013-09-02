package jlibbig.core;

import java.util.*;

/**
 * Bigraphs' signatures are immutable. To make a signature, users can use {@link SignatureBuilder}.
 */
public class Signature implements Iterable<Control>{

	final private Map<String,Control> ctrls = new HashMap<>();

	final protected String USID;

	public Signature(Collection<Control > controls) {
		this(null, controls);
	}

	public Signature(String usid, Collection<Control > controls) {
		for(Control  c : controls){
			if(ctrls.put(c.getName(), c) != null){
				throw new IllegalArgumentException("Controls must be uniquely named within the same signature");
			}
		}
		this.USID = (usid == null || usid.trim().length() == 0) ? UUID.randomUUID().toString() : usid;
	}

	public Signature(Control... controls) {
		this(null, controls);
	}

	public Signature(String usid, Control... controls) {
		for(Control  c : controls){
			if(ctrls.put(c.getName(), c) != null){
				throw new IllegalArgumentException("Controls must be uniquely named within the same signature");
			}
		}
		this.USID = (usid == null || usid.trim().length() == 0) ? UUID.randomUUID().toString() : usid;
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

	public String getUSID(){
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
	public Iterator<Control> iterator() {
		return Collections.unmodifiableMap(this.ctrls).values().iterator();
	}

	public int size() {
		return this.ctrls.size();
	}
}
