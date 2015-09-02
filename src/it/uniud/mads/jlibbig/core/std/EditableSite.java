package it.uniud.mads.jlibbig.core.std;


import it.uniud.mads.jlibbig.core.AbstractNamed;
import it.uniud.mads.jlibbig.core.BigraphHandler;
import it.uniud.mads.jlibbig.core.Owner;
import it.uniud.mads.jlibbig.core.attachedProperties.*;

class EditableSite implements EditableChild, Site {
	static final String PROPERTY_OWNER = "Owner";
	//public static final String PROPERTY_PARENT = "Parent";

	private EditableParent parent;  //redundant with parentProp

	private final DelegatedProperty.PropertySetter<Owner> ownerSetter;
	private final DelegatedProperty<Owner> ownerProp;

//	private final ProtectedProperty.ValueSetter<EditableParent> parentSetter;
//	private final ProtectedProperty<EditableParent> parentProp;

	private final ReplicationListenerContainer rep = new ReplicationListenerContainer();
	private final PropertyContainer props = new PropertyContainer();

	private final String name;
	
	EditableSite() {
		this.name = "S_" + AbstractNamed.generateName();
		this.ownerSetter = new DelegatedProperty.PropertySetter<>();
		this.ownerProp = new DelegatedProperty<Owner>(PROPERTY_OWNER, true, ownerSetter);
	
//		this.parentSetter = new ProtectedProperty.ValueSetter<EditableParent>();
//		this.parentProp = new ProtectedProperty<EditableParent>(PROPERTY_PARENT, parentSetter);
//	
		props.attachProperty(this.ownerProp);
//		props.attachProperty(this.parentProp);
	}

	EditableSite(EditableParent parent) {
		this();
		this.setParent(parent);
	}

	@Override
	public String toString() {
		Owner o = this.getOwner();
		if(o != null){
			BigraphHandler<?> h = (BigraphHandler<?>) o;
			int i = h.getRoots().indexOf(this);
			if(i >= 0)
				return i + ":s";
		}
		return this.name;
	}
	
	@Override
	public EditableParent getParent() {
		return parent;
	}

	@Override
	public Owner getOwner() {
		return this.ownerProp.get();
	}

	@Override
	public void setParent(EditableParent parent) {
		if(this.parent != parent){
			EditableParent old = this.parent;
			this.parent = parent;
			if (old != null) {
				old.removeChild(this);
			}
			if (parent != null) {
				parent.addChild(this);
				this.ownerSetter.set(parent
						.<Owner> getProperty(PROPERTY_OWNER));
			}
//			this.parentSetter.set(parent);
		}
	}

	/*
	@Override
	public Property<?> attachProperty(Property<?> prop) {
		if(prop == null)
			throw new IllegalArgumentException("Argument can not be null.");
		String name = prop.getName();
		if (PROPERTY_OWNER.equals(name) || PROPERTY_PARENT.equals(name))
			throw new IllegalArgumentException("Property '" + name
					+ "' can not be substituted");
		return props.attachProperty(prop);
	}

	@Override
	public <V> Property<V> detachProperty(Property<V> prop) {
		if(prop == null)
			throw new IllegalArgumentException("Argument can not be null.");
		return this.detachProperty(prop.getName());
	}

	@Override
	public <V> Property<V> detachProperty(String name) {
		if (PROPERTY_OWNER.equals(name) || PROPERTY_PARENT.equals(name))
			throw new IllegalArgumentException("Property '" + name
					+ "' can not be detached");
		return props.detachProperty(name);
	}

	@Override
	public <V> Property<V> getProperty(String name) {
		return props.getProperty(name);
	}

	@Override
	public  Collection<Property<?>> getProperties() {
		return props.getProperties();
	}

	@Override
	public Set<String> getPropertyNames() {
		return props.getPropertyNames();
	}
*/
	
	@Override
	public EditableSite replicate() {
		EditableSite copy = new EditableSite();
		rep.tellReplicated(this, copy);
		return copy;
	}

	@Override
	public void registerListener(ReplicationListener listener) {
		rep.registerListener(listener);
	}

	@Override
	public boolean isListenerRegistered(ReplicationListener listener) {
		return rep.isListenerRegistered(listener);
	}

	@Override
	public boolean unregisterListener(ReplicationListener listener) {
		return rep.unregisterListener(listener);
	}
	
	@Override
	public EditableSite getEditable() {
		return this;
	}

	@Override
	public boolean isParent() {
		return false;
	}

	@Override
	public boolean isChild() {
		return true;
	}

	@Override
	public boolean isRoot() {
		return false;
	}

	@Override
	public boolean isSite() {
		return true;
	}

	@Override
	public boolean isNode() {
		return false;
	}

}