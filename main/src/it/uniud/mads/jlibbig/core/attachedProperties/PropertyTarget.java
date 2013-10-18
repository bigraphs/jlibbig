package it.uniud.mads.jlibbig.core.attachedProperties;

import java.util.Collection;
import java.util.Set;

public interface PropertyTarget {

	public abstract Property<?> attachProperty(Property<?> prop);

	public abstract <V> Property<V> detachProperty(Property<V> prop);

	public abstract <V> Property<V> detachProperty(String name);

	public abstract <V> Property<V> getProperty(String name);
	
	public abstract Collection<Property<?>> getProperties();

	public abstract Set<String> getPropertyNames();

}