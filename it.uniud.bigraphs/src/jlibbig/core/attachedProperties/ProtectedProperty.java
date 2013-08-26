package jlibbig.core.attachedProperties;

@SuppressWarnings("unchecked")
public class ProtectedProperty<V> extends SimpleProperty<V> {

	public ProtectedProperty(String name,
			PropertyListener<V>... listeners) {
		super(name, listeners);
		super.writable = false;
	}
	
	public ProtectedProperty(String name, V value,
			PropertyListener<V>... listeners) {
		this(name, value, null, listeners);
	}
	
	public ProtectedProperty(String name, V value, ValueSetter<V> setter,
			PropertyListener<V>... listeners) {
		super(name, value, false, listeners);
		if(setter != null)
			setter.target = this;
	}
		
	public static class ValueSetter<V>{
		private ProtectedProperty<V> target;
		
		public ProtectedProperty<V> getTarget(){
			return target;
		}
		
		public V set(V value){
			return set(value,false);
		}
		
		public V set(V value, boolean silent){
			return target.set(value,silent);
		}
	}
}
