package jlibbig.core.attachedProperties;

import java.util.Set;

public interface PropertyTarget {

	public abstract Property<?> attachProperty(Property<?> prop);

	public abstract Property<?> detachProperty(Property<?> prop);

	public abstract Property<?> detachProperty(String name);

	public abstract Property<?> getProperty(String name);

	public abstract Set<String> getPropertyNames();

}