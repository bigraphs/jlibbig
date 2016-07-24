package it.uniud.mads.jlibbig.core.ldb;

import it.uniud.mads.jlibbig.core.Owned;
import it.uniud.mads.jlibbig.core.attachedProperties.Replicable;

interface EditableChild extends Child, Replicable, Owned {
    void setParent(EditableParent parent);

    @Override
    public abstract EditableParent getParent();

    @Override
    public abstract EditableChild replicate();
}
