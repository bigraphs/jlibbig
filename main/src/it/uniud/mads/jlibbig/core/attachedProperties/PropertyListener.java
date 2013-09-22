package it.uniud.mads.jlibbig.core.attachedProperties;

public interface PropertyListener<V> {

	public abstract void onChange(Property<? extends V> property, V oldValue,
			V newValue);

}
