package it.uniud.mads.jlibbig.core.attachedProperties;

/**
 * Properties created from this class delegate the values handling to some other
 * property. The property delegated can be set by means of a secret in the form
 * of an instance of {@link PropertySetter}. Optionally, the value of the delegated property
 * is cached to reduce the overhead of delegation chains. Listeners registering to
 * instances of this class are not passed to the delegated property. 
 * A property can be paired with at most one setter and this can not be changed;
 * if the setter is reused and paired with some other property, any previous
 * pairing will be lost.
 * 
 * @param <V>
 *            the type of the value held by the delegated property.
 */
public class DelegatedProperty<V> extends ProtectedProperty<V> {

	protected boolean cacheValue = false;

	private Property<V> prop;
	private final PropertyListener<? super V> lst;
	private boolean registered = false;

	@SafeVarargs
	public DelegatedProperty(String name, boolean cacheValue,
			PropertySetter<V> setter, PropertyListener<? super V>... listeners) {
		this(name, null, cacheValue, setter, listeners);
	}

	@SafeVarargs
	public DelegatedProperty(String name, Property<V> property,
			boolean cacheValue, PropertyListener<? super V>... listeners) {
		this(name, property, cacheValue, null, listeners);
	}

	@SafeVarargs
	public DelegatedProperty(String name, Property<V> property,
			PropertyListener<? super V>... listeners) {
		this(name, property, true, null, listeners);
	}

	@SafeVarargs
	public DelegatedProperty(String name, Property<V> property,
			PropertySetter<V> setter, PropertyListener<? super V>... listeners) {
		this(name, property, true, setter, listeners);
	}

	@SafeVarargs
	public DelegatedProperty(String name, Property<V> property,
			boolean cacheValue, PropertySetter<V> setter,
			PropertyListener<? super V>... listeners) {
		super(name, null, listeners);
		this.prop = property;
		this.cacheValue = cacheValue;
		if (cacheValue) {
			lst = new PropertyListener<V>() {
				@Override
				public void onChanged(Property<? extends V> property,
						V oldValue, V newValue) {
					set(newValue, false);
				}
			};
		} else {
			lst = new PropertyListener<V>() {
				@Override
				public void onChanged(Property<? extends V> property,
						V oldValue, V newValue) {
					tellChanged(DelegatedProperty.this, oldValue, newValue);
				}
			};
		}
		if (listeners.length > 0 && prop != null) {
			prop.registerListener(lst);
			registered = true;
			if (cacheValue)
				set(prop.get(), true);
		}
		if (setter != null)
			setter.target = this;
	}

	@Override
	public void registerListener(PropertyListener<? super V> listener) {
		super.registerListener(listener);
		if (!registered && prop != null) {
			prop.registerListener(lst);
			registered = true;
			if (cacheValue)
				set(prop.get(), true);
		}
	}

	@Override
	public boolean unregisterListener(PropertyListener<? super V> listener) {
		boolean r = super.unregisterListener(listener);
		if (registered && super.listeners.isEmpty()) {
			prop.unregisterListener(lst);
			registered = false;
		}
		return r;
	}

	/**
	 * @return the property delegated by this object.
	 */
	public Property<V> getProperty() {
		return prop;
	}

	/**
	 * Sets the property to delegate. Caching policies and listeners are
	 * preserved by changes.
	 * 
	 * @param prop
	 *            the new property to delegate.
	 */
	protected void setProperty(Property<V> prop) {
		if (this.prop != prop) {
			V oldValue = this.get();
			V newValue = (prop != null) ? prop.get() : null;
			if (this.prop != null && registered)
				this.prop.unregisterListener(lst);
			this.prop = prop;
			if (prop != null && registered)
				prop.registerListener(lst);
			if (cacheValue) {
				super.set(newValue, true);
			}
			if (oldValue != newValue)
				tellChanged(this, oldValue, this.get());
		}
	}

	@Override
	public V get() {
		if (cacheValue)
			return super.get();
		else {
			if (prop == null)
				return null;
			else
				return prop.get();
		}
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			if (prop != null && registered)
				prop.unregisterListener(lst);
		} finally {
			super.finalize();
		}
	}

	/**
	 * A secret used to write a {@link DelegatedProperty}. A setter can be
	 * paired with at most one target property and if reused any previous
	 * pairing will be lost.
	 * 
	 * @param <V>
	 *            the type of the value hold by the property.
	 */
	public final static class PropertySetter<V> {
		private DelegatedProperty<V> target;

		/**
		 * @return the instance the object can act upon.
		 */
		public DelegatedProperty<V> getTarget() {
			return target;
		}

		public void set(Property<V> prop) {
			target.setProperty(prop);
		}
	}
}
