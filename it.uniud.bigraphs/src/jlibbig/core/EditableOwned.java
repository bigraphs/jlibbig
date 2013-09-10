package jlibbig.core;

import jlibbig.core.abstractions.Owned;
import jlibbig.core.abstractions.Owner;

interface EditableOwned extends Owned{
	void setOwner(Owner value);
}
