package it.uniud.mads.jlibbig.core.attachedProperties;

/**
 * Properties created from this class can be written only by means of a secret
 * in the form of an instance of {@link ValueSetter}. A property can be paired 
 * with at most one setter and this can not be changed; if the setter is reused
 * and paired with some other property, any previous pairing will be lost.
 * 
 * @param <V>
 *            the type of the value hold by the property.
 */
public class ProtectedProperty<V> extends SimpleProperty<V> {

	@SafeVarargs
	public ProtectedProperty(String name, ValueSetter<V> setter,
			PropertyListener<? super V>... listeners) {
		super(name, listeners);
		super.readOnly = true;
		if (setter != null)
			setter.target = this;
	}

	@SafeVarargs
	public ProtectedProperty(String name, V value, ValueSetter<V> setter,
			PropertyListener<? super V>... listeners) {
		super(name, value, true, listeners);
		if (setter != null)
			setter.target = this;
	}

	/**
	 * A secret used to set the value of a protected property. A setter can be
	 * paired with at most one target property and if reused any previous
	 * pairing will be lost.
	 * 
	 * @param <V>
	 *            the type of the value hold by the property.
	 */
	public static class ValueSetter<V> {
		private ProtectedProperty<V> target;

		/**
		 * @return the protected property written by this setter.
		 */
		public ProtectedProperty<V> getTarget() {
			return target;
		}

		/**
		 * Sets the value of the target property.
		 * 
		 * @param value
		 *            the new value.
		 * @return the old value.
		 */
		public V set(V value) {
			return set(value, false);
		}

		/**
		 * Sets the value of the target property; optionally, the write can be
		 * carried out silently.
		 * 
		 * @param value
		 *            the new value.
		 * @param silent
		 *            if true the change will not be listened.
		 * @return the old value.
		 */
		public V set(V value, boolean silent) {
			return target.set(value, silent);
		}
	}
}
