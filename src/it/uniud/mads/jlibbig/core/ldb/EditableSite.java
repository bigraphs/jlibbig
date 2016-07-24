package it.uniud.mads.jlibbig.core.ldb;

import it.uniud.mads.jlibbig.core.BigraphHandler;
import it.uniud.mads.jlibbig.core.Owner;
import it.uniud.mads.jlibbig.core.attachedProperties.DelegatedProperty;
import it.uniud.mads.jlibbig.core.attachedProperties.PropertyContainer;
import it.uniud.mads.jlibbig.core.attachedProperties.ReplicationListener;
import it.uniud.mads.jlibbig.core.attachedProperties.ReplicationListenerContainer;
import it.uniud.mads.jlibbig.core.util.NameGenerator;

class EditableSite implements EditableChild, Site {
    static final String PROPERTY_OWNER = "Owner";

    private EditableParent parent;  //redundant with parentProp

    private final DelegatedProperty.PropertySetter<Owner> ownerSetter;
    private final DelegatedProperty<Owner> ownerProp;

    private final ReplicationListenerContainer rep = new ReplicationListenerContainer();
    private final PropertyContainer props = new PropertyContainer();

    private final String name;

    EditableSite() {
        this.name = "S_" + NameGenerator.DEFAULT.generate();
        this.ownerSetter = new DelegatedProperty.PropertySetter<>();
        this.ownerProp = new DelegatedProperty<>(PROPERTY_OWNER, true, ownerSetter);

        props.attachProperty(this.ownerProp);
    }

    EditableSite(EditableParent parent) {
        this();
        this.setParent(parent);
    }

    @Override
    public String toString() {
        Owner o = this.getOwner();
        if (o != null) {
            BigraphHandler<?> h = (BigraphHandler<?>) o;
            int i = h.getRoots().indexOf(this);
            if (i >= 0)
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
        if (this.parent != parent) {
            EditableParent old = this.parent;
            this.parent = parent;
            if (old != null) {
                old.removeChild(this);
            }
            if (parent != null) {
                parent.addChild(this);
                this.ownerSetter.set(parent.getProperty(PROPERTY_OWNER));
            }
        }
    }

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