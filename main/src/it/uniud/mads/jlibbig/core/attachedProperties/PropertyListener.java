package it.uniud.mads.jlibbig.core.attachedProperties;

public interface PropertyListener<V> {

	public abstract void onChanged(Property<? extends V> property, V oldValue,
			V newValue);

}
