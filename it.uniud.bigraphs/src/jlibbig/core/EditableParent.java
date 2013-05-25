package jlibbig.core;

import java.util.*;

interface EditableParent extends Parent, Replicable {
	Set<EditableChild> getEditableChildren();
	void addChild(EditableChild child);
	void removeChild(EditableChild child);
	EditableParent replicate();
}
