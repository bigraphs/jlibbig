package jlibbig;

import java.util.*;

interface EditableHandle extends Handle, Replicable{
	Set<EditablePoint> getEditablePoints();
	void linkPoint(EditablePoint point);
	void unlinkPoint(EditablePoint point);
	@Override
	EditableHandle replicate();
}
