package jlibbig.core.std;

import jlibbig.core.Owner;
import jlibbig.core.attachedProperties.DelegatedProperty;
import jlibbig.core.attachedProperties.PropertyContainer;
import jlibbig.core.attachedProperties.ReplicateListener;
import jlibbig.core.attachedProperties.ReplicateListenerContainer;

class EditableSite implements EditableChild, Site {
	static final String PROPERTY_OWNER = "Owner";

	private EditableParent parent;

	private final DelegatedProperty.PropertySetter<Owner> ownerSetter = new DelegatedProperty.PropertySetter<>();
	private final DelegatedProperty<Owner> owner = new DelegatedProperty<Owner>(
			PROPERTY_OWNER, true, ownerSetter);

	private final ReplicateListenerContainer rep = new ReplicateListenerContainer();
	private final PropertyContainer props = new PropertyContainer();

	EditableSite() {
		props.attachProperty(this.owner);
	}

	EditableSite(EditableParent parent) {
		props.attachProperty(this.owner);
		this.setParent(parent);
	}

	@Override
	public EditableParent getParent() {
		return parent;
	}

	@Override
	public Owner getOwner() {
		return this.owner.get();
	}

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
	public EditableSite replicate() {
		EditableSite copy = new EditableSite();
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