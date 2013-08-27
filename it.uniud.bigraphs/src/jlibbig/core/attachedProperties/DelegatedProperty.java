package jlibbig.core.attachedProperties;

@SuppressWarnings("unchecked")
public class DelegatedProperty<V> extends ProtectedProperty<V> {

	protected boolean cacheValue = false;
	
	protected Property<V> prop;
	protected final PropertyListener<V> lst;
	protected boolean registered = false;
	
	public DelegatedProperty(String name, boolean cacheValue, PropertySetter<V> setter, PropertyListener<V>... listeners) {
		this(name, null,cacheValue,setter,listeners);
	}
	
	public DelegatedProperty(String name, Property<V> property, boolean cacheValue, PropertyListener<V>... listeners) {
		this(name, property,cacheValue,null,listeners);
	}
	
	public DelegatedProperty(String name, Property<V> property, PropertyListener<V>... listeners) {
		this(name, property,true,null,listeners);
	}
	
	public DelegatedProperty(String name, Property<V> property, PropertySetter<V> setter, PropertyListener<V>... listeners) {
		this(name, property,true,setter,listeners);
	}
	
	public DelegatedProperty(String name, Property<V> property, boolean cacheValue, PropertySetter<V> setter,
			PropertyListener<V>... listeners) {
		super(name,null,listeners);
		this.prop = property;
		this.cacheValue = cacheValue;
		if(cacheValue){
			lst = new PropertyListener<V>(){
				public void onChange(Property<V> property, V oldValue, V newValue){
					set(newValue,true);
				}
			};
		}else{
			lst = new PropertyListener<V>(){
				public void onChange(Property<V> property, V oldValue, V newValue){
					tellChanged(DelegatedProperty.this,oldValue,newValue);
				}
			};
		}
		if(listeners.length > 0 && prop != null){
			prop.registerListener(lst);
			registered = true;
			if(cacheValue)
				set(prop.get(),true);
		}
		if(setter != null)
			setter.target = this;
	}

	public void registerListener(PropertyListener<V> listener){
		super.registerListener(listener);
		if(!registered && prop != null){
			prop.registerListener(lst);
			registered = true;
			if(cacheValue)
				set(prop.get(),true);
		}
	}

	public boolean unregisterListener(PropertyListener<V> listener){
		boolean r = super.unregisterListener(listener);
		if(registered && super.listeners.size() == 0){
			prop.unregisterListener(lst);
			registered = false;
		}
		return r;
	}

	public Property<V> getProperty(){
		return prop;
	}
	
	protected void setProperty(Property<V> prop){
		if(this.prop != prop){
			V oldValue = this.get();
			if(this.prop != null && registered)
				this.prop.unregisterListener(lst);
			this.prop = prop;
			if(this.prop != null && registered)
				this.prop.registerListener(lst);
			if(cacheValue && registered){
				super.set(this.prop.get(),true);
			}
			tellChanged(this,oldValue,this.get());
		}
	}
	
	public V get(){
		if(prop == null)
			return null;
		else if(cacheValue)
			return super.get();
		else
			return prop.get();
	}
	
	protected void finalize() throws Throwable {
	     try {
	    	 if(prop != null && registered)
	    		 prop.unregisterListener(lst);
	     } finally {
	         super.finalize();
	     }
	 }
	
	public static class PropertySetter<V>{
		private DelegatedProperty<V> target;
		
		public DelegatedProperty<V> getTarget(){
			return target;
		}
		
		public void set(Property<V> prop){
			target.setProperty(prop);
		}
	}
}
