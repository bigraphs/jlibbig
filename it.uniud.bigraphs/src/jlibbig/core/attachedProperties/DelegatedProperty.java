package jlibbig.core.attachedProperties;

public class DelegatedProperty<V> extends ProtectedProperty<V> {

	protected boolean cacheValue = false;

	protected Property<V> prop;
	protected final PropertyListener<? super V> lst;
	protected boolean registered = false;

    @SafeVarargs
	public DelegatedProperty(String name, boolean cacheValue, PropertySetter<V> setter, PropertyListener<? super V>... listeners) {
		this(name, null,cacheValue,setter,listeners);
    }

    @SafeVarargs
	public DelegatedProperty(String name, Property<V> property, boolean cacheValue, PropertyListener<? super V>... listeners) {
		this(name, property,cacheValue,null,listeners);
	}

    @SafeVarargs
	public DelegatedProperty(String name, Property<V> property, PropertyListener<? super V>... listeners) {
		this(name, property,true,null,listeners);
	}

    @SafeVarargs
	public DelegatedProperty(String name, Property<V> property, PropertySetter<V> setter, PropertyListener<? super V>... listeners) {
		this(name, property,true,setter,listeners);
	}

    @SafeVarargs
	public DelegatedProperty(String name, Property<V> property, boolean cacheValue, PropertySetter<V> setter,
			PropertyListener<? super V>... listeners) {
		super(name,null,listeners);
		this.prop = property;
		this.cacheValue = cacheValue;
		if(cacheValue){
			lst = new PropertyListener<V>(){
                @Override
				public void onChange(Property<? extends V> property, V oldValue, V newValue){
					set(newValue,true);
				}
			};
		}else{
			lst = new PropertyListener<V>(){
                @Override
				public void onChange(Property<? extends V> property, V oldValue, V newValue){
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

    @Override
	public void registerListener(PropertyListener<? super V> listener){
		super.registerListener(listener);
		if(!registered && prop != null){
			prop.registerListener(lst);
			registered = true;
			if(cacheValue)
				set(prop.get(),true);
		}
	}

    @Override
	public boolean unregisterListener(PropertyListener<? super V> listener){
		boolean r = super.unregisterListener(listener);
		if(registered && super.listeners.isEmpty()){
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

    @Override
	public V get(){
		if(prop == null)
			return null;
		else if(cacheValue)
			return super.get();
		else
			return prop.get();
	}

    @Override
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
