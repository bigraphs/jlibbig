package it.uniud.mads.jlibbig.core.attachedProperties;

import java.util.Collection;

/**
 * The interface provides methods for dynamically extending objects by attaching
 * and detaching properties. Properties are instances of the {@link Property}
 * and changes in their values can be observed by means of
 * {@link PropertyListener}. Use the {@link PropertyContainer} to implement this
 * interface.
 */
public interface PropertyTarget {

	/**
	 * Attaches a property to the object. Properties with the same name are
	 * replaced.
	 * 
	 * @param prop
	 *            the property to be attached.
	 * @return the old property (if any).
	 */
	public abstract Property<?> attachProperty(Property<?> prop);

	/**
	 * Detaches a property from the object. The method return the property if it
	 * was actually attached, otherwise null.
	 * 
	 * @param prop
	 *            the property to be detached.
	 * @return the detached property.
	 */
	public abstract <V> Property<V> detachProperty(Property<V> prop);

	/**
	 * Detaches a property from the object by its name.
	 * 
	 * @param name
	 *            the name of the property to be detached.
	 * @return the detached property.
	 */
	public abstract <V> Property<V> detachProperty(String name);

	/**
	 * Gets a property attached to the object by its name, null if there is no
	 * property for the given name.
	 * 
	 * @param name
	 *            the name of the property to be retieved.
	 * @return the property.
	 */
	public abstract <V> Property<V> getProperty(String name);

	/**
	 * The collection of all properties attached to the object.
	 * 
	 * @return the property collection.
	 */
	public abstract Collection<Property<?>> getProperties();

	/**
	 * The names properties attached to the object.
	 * 
	 * @return the names collection.
	 */
	public abstract Collection<String> getPropertyNames();

}