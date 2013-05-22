package jlibbig;

import java.util.*;

public class Signature implements Set<BigraphControl>{

	final private Map<String,BigraphControl> ctrls = new HashMap<>();

	Signature(Collection<BigraphControl > controls) {
		for(BigraphControl  c : controls){
			if(ctrls.containsKey(c.getName())){
				throw new IllegalArgumentException("Controls must be uniquely named within the same signature");
			}else{
				ctrls.put(c.getName(), c);
			}
		}
	}
		
	public BigraphControl getByName(String name){
		return ctrls.get(name);
	}
		
	@Override
	public String toString() {
		return "Signature " + ctrls.values();
	}

	
	@Override
	public boolean add(BigraphControl  arg0) {
		throw new UnsupportedOperationException("Signatures are read-only sets");
	}

	@Override
	public boolean addAll(Collection<? extends BigraphControl > arg0) {
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
	public Iterator<BigraphControl> iterator() {
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
