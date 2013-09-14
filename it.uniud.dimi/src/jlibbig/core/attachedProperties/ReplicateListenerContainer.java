package jlibbig.core.attachedProperties;

import java.util.*;

public class ReplicateListenerContainer {

	private List<ReplicateListener> _listeners = new LinkedList<>();
	protected List<ReplicateListener> listeners = Collections
			.unmodifiableList(_listeners);

	public ReplicateListenerContainer(ReplicateListener... listeners) {
		this._listeners.addAll(Arrays.asList(listeners));
	}

	public void registerListener(ReplicateListener listener) {
		if (_listeners.contains(listener))
			return;
		_listeners.add(listener);
	}

	public boolean unregisterListener(ReplicateListener listener) {
		return _listeners.remove(listener);
	}

	public void tell(Replicable original, Replicable copy) {
		ListIterator<ReplicateListener> li = _listeners.listIterator();
		while (li.hasNext()) {
			li.next().onReplicate(original, copy);
		}
	}

}
