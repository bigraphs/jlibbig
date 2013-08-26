package jlibbig.core;

import java.util.*;

public class ReplicateListenerContainer {

	protected List<ReplicateListener> listeners = new LinkedList<>();
	
	public ReplicateListenerContainer(ReplicateListener... listeners){
		for(ReplicateListener l : listeners){
			this.listeners.add(l);
		}
	}
	
	public void registerListener(ReplicateListener listener){
		if(listeners.contains(listeners))
			return;
		listeners.add(listener);
	}
	
	public boolean unregisterListener(ReplicateListener listener){
		return listeners.remove(listener);
	}
	
	public void tell(Replicable original, Replicable copy){
		
	}
	
}
