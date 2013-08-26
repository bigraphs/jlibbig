package jlibbig.core.attachedProperties;

public interface PropertyTarget {

	public abstract Property<?> attachProperty(Property<?> prop);

	public abstract Property<?> detachProperty(Property<?> prop);

	public abstract Property<?> detachProperty(String name);

	public abstract Property<?> getProperty(String name);

}