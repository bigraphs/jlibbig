package jlibbig.core;

import java.util.*;

import jlibbig.core.abstractions.Owner;

class EditableOuterName extends EditableLinkFacet implements OuterName,
		EditableNamed, EditableHandle {

	private Collection<EditablePoint> points = Collections
			.newSetFromMap(new IdentityHashMap<EditablePoint, Boolean>());
	private final Collection<? extends Point> ro_points = Collections
			.unmodifiableCollection(this.points);
	private Owner owner;

	EditableOuterName(String name) {
		super(name);
	}

	EditableOuterName() {
		super();
	}

	@Override
	public Collection<? extends Point> getPoints() {
		return this.ro_points;
	}

	@Override
	public Collection<EditablePoint> getEditablePoints() {
		return this.points;
	}

	@Override
	public void linkPoint(EditablePoint point) {
		if (point == null)
			return;
		this.points.add(point);
		if (this != point.getHandle()) {
			point.setHandle(this);
		}
	}

	@Override
	public void unlinkPoint(EditablePoint point) {
		if (point == null)
			return;
		if (this.points.remove(point) && this == point.getHandle())
			point.setHandle(null);
	}

	@Override
	public EditableOuterName replicate() {
		EditableOuterName copy = new EditableOuterName(this.getName());
		rep.tell(this, copy);
		return copy;
	}

	@Override
	public Owner getOwner() {
		return this.owner;
	}

	@Override
	public void setOwner(Owner value) {
		this.owner = value;
	}

	@Override
	public EditableOuterName getEditable() {
		return this;
	}

	@Override
	public boolean isHandle() {
		return true;
	}

	@Override
	public boolean isPoint() {
		return false;
	}

	@Override
	public boolean isPort() {
		return false;
	}

	@Override
	public boolean isInnerName() {
		return false;
	}

	@Override
	public boolean isOuterName() {
		return true;
	}

	@Override
	public boolean isEdge() {
		return false;
	}
}