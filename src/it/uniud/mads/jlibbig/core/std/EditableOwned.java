package it.uniud.mads.jlibbig.core.std;

import it.uniud.mads.jlibbig.core.Owned;
import it.uniud.mads.jlibbig.core.Owner;

interface EditableOwned extends Owned {
	void setOwner(Owner value);
}
