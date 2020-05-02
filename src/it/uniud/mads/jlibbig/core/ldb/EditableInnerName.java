package it.uniud.mads.jlibbig.core.ldb;

import it.uniud.mads.jlibbig.core.Owner;

class EditableInnerName extends EditableLinkFacet implements InnerName, EditableNamed, EditablePoint {

    private EditableHandle handle;

    EditableInnerName() {
    }

    EditableInnerName(String name) {
        super(name);
    }

    EditableInnerName(EditableHandle handle) {
        setHandle(handle);
    }

    EditableInnerName(String name, EditableHandle handle) {
        super(name);
        setHandle(handle);
    }

    @Override
    public EditableHandle getHandle() {
        return this.handle;
    }

    @Override
    public Owner getOwner() {
        return (handle == null) ? null : handle.getOwner();
    }

    @Override
    public EditableInnerName replicate() {
        EditableInnerName copy = new EditableInnerName(this.getName());
        rep.tellReplicated(this, copy);
        return copy;
    }

    @Override
    public EditableInnerName getEditable() {
        return this;
    }

    @Override
    public boolean isHandle() {
        return false;
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
    public boolean isPoint() {
        return true;
    }

    @Override
    public boolean isPort() {
        return false;
    }

    @Override
    public boolean isInnerName() {
        return true;
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
