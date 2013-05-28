package jlibbig.core;

import java.util.*;

interface EditableParent extends Parent, Replicable, Owned {
	Set<EditableChild> getEditableChildren();
	void addChild(EditableChild child);
	void removeChild(EditableChild child);
	EditableParent replicate();
}
