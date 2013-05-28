package jlibbig.core;

import java.util.*;

interface EditableHandle extends Handle, Replicable, Owned, EditableOwned{
	Set<EditablePoint> getEditablePoints();
	void linkPoint(EditablePoint point);
	void unlinkPoint(EditablePoint point);
	@Override
	EditableHandle replicate();
}
