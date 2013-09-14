package jlibbig.core.std;

import jlibbig.core.Owned;
import jlibbig.core.Owner;

interface EditableOwned extends Owned {
	void setOwner(Owner value);
}
