package jlibbig.core;

import java.util.*;

class ReplicateListenerContainer {

	private List<ReplicateListener> _listeners = new LinkedList<>();
	protected List<ReplicateListener> listeners = Collections.unmodifiableList(_listeners);
	
	public ReplicateListenerContainer(ReplicateListener... listeners){
		for(ReplicateListener l : listeners){
			this._listeners.add(l);
		}
	}
	
	public void registerListener(ReplicateListener listener){
		if(_listeners.contains(_listeners))
			return;
		_listeners.add(listener);
	}
	
	public boolean unregisterListener(ReplicateListener listener){
		return _listeners.remove(listener);
	}
	
	public void tell(Replicable original, Replicable copy){
		ListIterator<ReplicateListener> li = _listeners.listIterator();
		while(li.hasNext()){
			li.next().onReplicate(original, copy);
		}
	}
	
}
