package it.uniud.mads.jlibbig.core.std;

import java.util.*;

import it.uniud.mads.jlibbig.core.AbstractNamed;
import it.uniud.mads.jlibbig.core.Owner;
import it.uniud.mads.jlibbig.core.attachedProperties.ReplicateListener;
import it.uniud.mads.jlibbig.core.attachedProperties.ReplicateListenerContainer;

/**
 * Edges of a link graph. <br />
 * They can be linked to innernames and edges.
 * 
 */
class EditableEdge implements Edge, EditableHandle, ReplicableEx {
	private String name;

	private Collection<EditablePoint> points = Collections
			.newSetFromMap(new IdentityHashMap<EditablePoint, Boolean>());
	private final Collection<? extends Point> ro_points = Collections
			.unmodifiableCollection(this.points);
	private Owner owner;
	private final ReplicateListenerContainer rep = new ReplicateListenerContainer();

	EditableEdge() {
		name = "E_" + AbstractNamed.generateName();
	}

	EditableEdge(Owner owner) {
		this();
		this.setOwner(owner);
	}

	@Override
	public String toString() {
		return this.name;
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
		this.points.remove(point);
		if (this == point.getHandle())
			point.setHandle(null);
	}

	@Override
	public EditableEdge replicate() {
		EditableEdge copy = new EditableEdge();
		rep.tell(this, copy);
		return copy;
	}

	@Override
	public void registerListener(ReplicateListener listener) {
		rep.registerListener(listener);
	}

	@Override
	public boolean unregisterListener(ReplicateListener listener) {
		return rep.unregisterListener(listener);
	}

	@Override
	public Owner getOwner() {
		return this.owner;
	}
	
	String getName() {
		return name;
	}

	@Override
	public void setOwner(Owner value) {
		this.owner = value;
	}

	@Override
	public int hashCode() {
		final int prime = 83;
		return prime * name.hashCode();
	}

	@Override
	public EditableEdge getEditable() {
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
		return false;
	}

	@Override
	public boolean isEdge() {
		return true;
	}

}