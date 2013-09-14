package jlibbig.core.attachedProperties;

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

	public static class ValueSetter<V> {
		private ProtectedProperty<V> target;

		public ProtectedProperty<V> getTarget() {
			return target;
		}

		public V set(V value) {
			return set(value, false);
		}

		public V set(V value, boolean silent) {
			return target.set(value, silent);
		}
	}
}
