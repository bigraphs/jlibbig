package jlibbig.core.attachedProperties;

public interface Property<V> {

	public abstract boolean isReadOnly();

	public abstract void registerListener(PropertyListener<V> listener);

	public abstract boolean isListenerRegistered(
			PropertyListener<V> listener);
	
	public abstract boolean unregisterListener(
			PropertyListener<V> listener);

	public abstract V get();

	public abstract V set(V value);

	public abstract String getName();

}