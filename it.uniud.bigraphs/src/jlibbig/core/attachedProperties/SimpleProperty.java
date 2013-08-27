package jlibbig.core.attachedProperties;

import java.util.*;

public class SimpleProperty<V> extends Property<V>{
	
	private V value;
	private final String name;
	
	private List<PropertyListener<V>> _listeners = new LinkedList<>();
	
	/**
	 * A list holding all the listener registered for this property
	 */
	protected List<PropertyListener<V>> listeners = Collections.unmodifiableList(_listeners);
	
	/**
	 * A flag indicating whatever the property is writable
	 */
	protected boolean readOnly = true;
	
	public SimpleProperty(String name, @SuppressWarnings("unchecked") PropertyListener<V>... listeners){
		this.name = name;
		for(PropertyListener<V> l : listeners){
			this._listeners.add(l);
		}
	}
	
	public SimpleProperty(String name, V value, @SuppressWarnings("unchecked") PropertyListener<V>... listeners){
		this(name,value,false,listeners);
	}
	
	public SimpleProperty(String name, V value, boolean readOnly, @SuppressWarnings("unchecked") PropertyListener<V>... listeners){
		this.name = name;
		this.value = value;
		this.readOnly = readOnly;
		for(PropertyListener<V> l : listeners){
			this._listeners.add(l);
		}
	}
	
	public SimpleProperty(String name, V value, boolean writable, Collection<PropertyListener<V>> listeners){
		this.name = name;
		this.value = value;
		this.readOnly = writable;
		this._listeners.addAll(listeners);
	}
	
	/* (non-Javadoc)
	 * @see jlibbig.core.AttachedProperty#isReadOnly()
	 */
	@Override
	public boolean isReadOnly(){
		return this.readOnly;
	}
	
	@Override
	public boolean isListenerRegistered(PropertyListener<V> listener) {
		return _listeners.contains(listener);
	}
	
	/* (non-Javadoc)
	 * @see jlibbig.core.AttachedProperty#registerListener(jlibbig.core.AttachedPropertyListener)
	 */
	@Override
	public void registerListener( PropertyListener<V> listener){
		if(_listeners.contains(_listeners))
			return;
		_listeners.add(listener);
	}
	
	/* (non-Javadoc)
	 * @see jlibbig.core.AttachedProperty#unregisterListener(jlibbig.core.AttachedPropertyListener)
	 */
	@Override
	public boolean unregisterListener(PropertyListener<V> listener){
		return _listeners.remove(listener);
	}
	
	/* (non-Javadoc)
	 * @see jlibbig.core.AttachedProperty#get()
	 */
	@Override
	public V get(){
		return value;
	}

	/* (non-Javadoc)
	 * @see jlibbig.core.AttachedProperty#set(V)
	 */
	@Override
	public V set(V value){
		if(isReadOnly())
			throw new UnsupportedOperationException("Property '" + getName() + "' is read only.");
		return set(value,false);
	}
	
	protected V set(V value,boolean silent){
		V old = this.value;
		this.value = value;
		if(!silent){
			tellChanged(this, old, value);
		}
		return old;
	}

	protected void tellChanged(Property<V> property, V oldValue, V newValue){
		ListIterator<PropertyListener<V>> li = _listeners.listIterator();
		while(li.hasNext()){
			li.next().onChange(property, oldValue, newValue);
		}
	}
	
	/* (non-Javadoc)
	 * @see jlibbig.core.AttachedProperty#getName()
	 */
	@Override
	public String getName() {
		return this.name;
	}

}
