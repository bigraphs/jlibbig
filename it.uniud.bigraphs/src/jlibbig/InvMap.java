package jlibbig;

import java.util.*;



class InvMap<A,B> implements Map<A,B>{
	
	// map 
	private Map<A,B> _map = new HashMap<>();
	// reverse map 
	private Map<B,Set<A>> _inv = null;
	
	protected InvMap(){}
	
	protected InvMap(Map<? extends A,? extends B> map){
		_map.putAll(map);
	}
	
	private void computeInv(){
		if(_inv!=null)
			_inv = new HashMap<>();
		for(B b : _map.values()){
			_inv.put(b, new HashSet<A>());
		}
		for(A a : _map.keySet()){
			_inv.get(_map.get(a)).add(a);
		}	
	}
			
	@Override
	public void clear() {
		_map.clear();
		if(_inv!=null) _inv.clear();
	}

	@Override
	public boolean containsKey(Object arg) {
		return _map.containsKey(arg);
	}

	@Override
	public boolean containsValue(Object arg) {
		if(_inv!=null)
			computeInv(); 
		return _inv.containsKey(arg);
	}

	@Override
	public Set<java.util.Map.Entry<A, B>> entrySet() {
		return _map.entrySet();
	}

	@Override
	public B get(Object arg) {
		return _map.get(arg);
	}
	
	public Set<A> getKeys(B arg) {
		if(_inv!=null)
			computeInv(); 
		return _inv.get(arg);
	}
	
	public B getValue(A arg) {
		return _map.get(arg);
	}

	@Override
	public boolean isEmpty() {
		return _map.isEmpty();
	}

	@Override
	public Set<A> keySet() {
		return _map.keySet();
	}

	@Override
	public B put(A arg0, B arg1) {
		if(_inv != null){
			Set<A> set = _inv.get(arg0);
			if(set == null){
				set = new HashSet<A>();
				_inv.put(arg1, set);
			}
			set.add(arg0);
		}
		return _map.put(arg0, arg1);
	}

	@Override
	public void putAll(Map<? extends A, ? extends B> arg) {
		_map.putAll(arg);
		if(_inv != null){
			for(A a : arg.keySet()){
				B b = arg.get(a);
				Set<A> set = _inv.get(b);
				if(set == null){
					set = new HashSet<A>();
					_inv.put(b, set);
				}
				set.add(a);
			}
		}
	}

	@Override
	public B remove(Object arg) {
		B b = _map.remove(arg);
		if(b != null && _inv != null){
			Set<A> set = _inv.get(arg);
			set.remove(arg);
			if(set.isEmpty())
				_inv.remove(b);
		}
		return b;
	}

	@Override
	public int size() {
		return _map.size();
	}

	@Override
	public Collection<B> values() {
		return _map.values();
	}

}
