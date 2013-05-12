package jlibbig;

import java.util.*;

public class Signature<C extends GraphControl> implements Set<C>{

	final private Map<String,C> ctrls = new HashMap<>();

	protected Signature() {}
	
	public Signature(Collection<C> controls) {
		for(C c : controls){
			if(ctrls.containsKey(c.getName())){
				throw new IllegalArgumentException("Controls must be uniquely named within the same signature");
			}else{
				ctrls.put(c.getName(), c);
			}
		}
	}
		
	public C getByName(String name){
		return ctrls.get(name);
	}
		
	@Override
	protected synchronized Signature<C> clone() {
		return new Signature<>(this.ctrls.values());
	}
	
	@Override
	public String toString() {
		return "Signature " + ctrls.values();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ctrls == null) ? 0 : ctrls.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Signature<?> other = (Signature<?>) obj;
		if (ctrls == null) {
			if (other.ctrls != null)
				return false;
		} else if (!ctrls.equals(other.ctrls))
			return false;
		return true;
	}

	void extendWith(C control) {
		if(this.ctrls.containsKey(control.getName()))
			throw new IllegalArgumentException("Duplicated control");
		this.ctrls.put(control.getName(), control);
	}

	void extendWith(Collection<? extends C> controls) {
		for(C ctrl : controls)
			extendWith(ctrl);
	}

	
	@Override
	public boolean add(C arg0) {
		throw new UnsupportedOperationException("Signatures are read-only sets");
	}

	@Override
	public boolean addAll(Collection<? extends C> arg0) {
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
	public Iterator<C> iterator() {
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
