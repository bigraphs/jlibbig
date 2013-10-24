package it.uniud.mads.jlibbig.core.attachedProperties;

/**
 *
 * @param <V> the type of the value hold by the property.
 */
public abstract class Property<V> {

	private final String name;
	
	public Property(String name){
		this.name = name;
	}
	
	public abstract boolean isReadOnly();

	public abstract boolean isListenerRegistered(PropertyListener<? super V> listener);

	public abstract void registerListener(PropertyListener<? super V> listener);

	public abstract boolean unregisterListener(PropertyListener<? super V> listener);
	
	public abstract V get();

	public abstract V set(V value);

	public final String getName(){
		return this.name;
	}

	protected void onAttach(PropertyTarget target) {
	};

	protected void onDetach(PropertyTarget target) {
	}

	@Override
	public String toString() {
		return "Property '" + getName() + "'=" + get();
	};
		
}