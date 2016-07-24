package it.uniud.mads.jlibbig.core.ldb;

import it.uniud.mads.jlibbig.core.Owned;
import it.uniud.mads.jlibbig.core.Owner;

interface EditableOwned extends Owned {
    void setOwner(Owner value);
}
