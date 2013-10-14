package it.uniud.mads.jlibbig.core.std;

import java.util.*;

import it.uniud.mads.jlibbig.core.AbstractNamed;
import it.uniud.mads.jlibbig.core.Owner;
import it.uniud.mads.jlibbig.core.attachedProperties.*;

/**
 * Describes a node of a bigraph. <br />
 * Every node must have its control.
 */
class EditableNode implements Node, EditableParent, EditableChild {
	public static final String PROPERTY_OWNER = "Owner";
	//public static final String PROPERTY_ALIAS = "Alias";

	private Control control;
	private final List<EditablePort> ports;
	private EditableParent parent;
	private Collection<EditableChild> children;
	private final List<? extends Port> ro_ports;
	private final Collection<? extends Child> ro_chd;
	private String name;

	private final DelegatedProperty.PropertySetter<Owner> ownerSetter = new DelegatedProperty.PropertySetter<>();
	private final DelegatedProperty<Owner> owner = new DelegatedProperty<Owner>(
			PROPERTY_OWNER, true, ownerSetter);

	//private final ReplicatingProperty<String> alias = new ReplicatingProperty<String>(PROPERTY_ALIAS); 
	
	private final ReplicateListenerContainer rep = new ReplicateListenerContainer();
	private final PropertyContainer props = new PropertyContainer(this);

	EditableNode(Control control) {
		this.name = "N_" + AbstractNamed.generateName();
		this.control = control;
		List<EditablePort> ports = new ArrayList<>();
		for (int i = 0; i < control.getArity(); i++) {
			ports.add(new EditablePort(i));
		}
		this.ports = Collections.unmodifiableList(ports);
		this.children = new HashSet<>();
		this.ro_ports = Collections.unmodifiableList(this.ports);
		this.ro_chd = Collections.unmodifiableCollection(this.children);

		//this.alias.set(name);
		
		props.attachProperty(this.owner);
		//props.attachProperty(this.alias);
	}

	EditableNode(Control control, EditableParent parent) {
		this(control);
		setParent(parent);
	}

	EditableNode(Control control, EditableParent parent,
			List<? extends Handle> handles) {
		this(control, parent);
		for (int i = 0; i < Math.min(handles.size(), control.getArity()); i++) {
			this.ports.get(i).setHandle((EditableHandle) handles.get(i));
		}
	}

	EditableNode(Control control, EditableParent parent,
			EditableHandle... handles) {
		this(control,parent);
		for (int i = 0; i < Math.min(handles.length, control.getArity()); i++) {
			this.ports.get(i).setHandle(handles[i]);
		}
	}
	
	public String getName(){
		return name;
	}

//	public String getAlias(){
//		return alias.get();
//	}
//	
//
//	public String setAlias(String value){
//		return alias.set(value);
//	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
//		String alias = getAlias();
//		if(alias == null){
			builder.append(this.name)
			.append(':')
			.append(this.control.getName());
//		}else{
//			builder.append(alias)
//			.append('(')
//			.append(this.name)
//			.append(':')
//			.append(this.control.getName())
//			.append(')');
//		}
		return builder.toString();
	}

	@Override
	public EditableParent getParent() {
		return this.parent;
	}

	@Override
	public Collection<? extends Child> getChildren() {
		return this.ro_chd;
	}

	@Override
	public List<? extends Port> getPorts() {
		return this.ro_ports;
	}

	public List<EditablePort> getPortsForEdit() {
		return this.ports;
	}

	@Override
	public EditablePort getPort(int index) {
		return this.ports.get(index);
	}

	@Override
	public Control getControl() {
		return this.control;
	}

	// public void setControl(Control value){
	// throw new UnsupportedOperationException("Not implemented yet.");
	// //this.control = value;
	// }

	@Override
	public void setParent(EditableParent parent) {
		if (this.parent != null) {
			if (this.parent != parent) {
				EditableParent p = this.parent;
				this.parent = parent;
				p.removeChild(this);
			}
		}
		this.parent = parent;
		if (this.parent != null) {
			this.parent.addChild(this);
			this.ownerSetter.set(this.parent
					.<Owner> getProperty(PROPERTY_OWNER));
		}
	}

	@Override
	public void addChild(EditableChild child) {
		if (child == null)
			return;
		this.children.add(child);
		if (this != child.getParent()) {
			child.setParent(this);
		}
	}

	@Override
	public void removeChild(EditableChild child) {
		if (child == null)
			return;
		this.children.remove(child);
		if (this == child.getParent())
			child.setParent(null);
	}

	@Override
	public Collection<EditableChild> getEditableChildren() {
		return this.children;
	}

	@Override
	public EditableRoot getRoot() {
		return this.parent.getRoot();
	}

	@Override
	public EditableNode replicate() {
		EditableNode copy = new EditableNode(this.control);
		rep.tell(this, copy);
		return copy;
	}

	@Override
	public void registerListener(ReplicateListener listener) {
		rep.registerListener(listener);
	}

	@Override
	public boolean unregisterListener(ReplicateListener listener) {
		return rep.unregisterListener(listener);
	}

	@Override
	public Property<?> attachProperty(Property<?> prop) {
		if(prop == null)
			throw new IllegalArgumentException("Argument can not be null.");
		String name = prop.getName();
		if (name.equals(PROPERTY_OWNER))// || name.equals(PROPERTY_ALIAS))
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
		if (PROPERTY_OWNER.equals(name))// || PROPERTY_ALIAS.equals(name))
			throw new IllegalArgumentException("Property '" + name
					+ "' can not be detached");
		return props.detachProperty(name);
	}

	@Override
	public <V> Property<V> getProperty(String name) {
		return props.getProperty(name);
	}

	@Override
	public Set<String> getPropertyNames() {
		return props.getPropertyNames();
	}

	@Override
	public Owner getOwner() {
		return this.owner.get();
	}

	@Override
	public EditableNode getEditable() {
		return this;
	}

	@Override
	public boolean isParent() {
		return true;
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
		return false;
	}

	@Override
	public boolean isNode() {
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 41;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	public class EditablePort implements Port, EditablePoint {
		private final int number;
		private EditableHandle handle;

		private EditablePort(int number) {
			this.number = number;
		}

		@Override
		public EditableNode getNode() {
			return EditableNode.this;
		}

		@Override
		public String toString() {
			return number + "@" + EditableNode.this;
		}

		@Override
		public int getNumber() {
			return this.number;
		}

		@Override
		public EditableHandle getHandle() {
			return handle;
		}

		@Override
		public void setHandle(EditableHandle handle) {
			if (this.handle == handle)
				return;
			EditableHandle old = this.handle;
			this.handle = null;
			if (old != null)
				old.unlinkPoint(this);
			this.handle = handle;
			if (handle != null) {
				handle.linkPoint(this);
			}
		}

		@Override
		public Owner getOwner() {
			return (handle != null) ? handle.getOwner() : EditableNode.this
					.getOwner();
		}

		@Override
		public EditablePort getEditable() {
			return this;
		}

		@Override
		public boolean isHandle() {
			return false;
		}

		@Override
		public boolean isPoint() {
			return true;
		}

		@Override
		public boolean isPort() {
			return true;
		}

		@Override
		public boolean isInnerName() {
			return false;
		}

		@Override
		public boolean isOuterName() {
			return false;
		}

		@Override
		public boolean isEdge() {
			return false;
		}
	}

}
