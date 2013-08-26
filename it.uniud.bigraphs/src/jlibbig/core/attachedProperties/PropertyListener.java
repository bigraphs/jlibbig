package jlibbig.core.attachedProperties;

public interface PropertyListener<V> {

	public abstract void onChange(Property<V> property, V oldValue, V newValue);

}
