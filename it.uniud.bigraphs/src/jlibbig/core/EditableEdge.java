package jlibbig.core;

import java.util.*;

/**
 * Edges of a link graph. <br />
 * They can be linked to innernames and edges.
 *
 */
class EditableEdge implements Edge, EditableHandle, ReplicableEx{
	private String name;
	
	private Set<EditablePoint> points = new HashSet<>();
	private final Set<? extends Point> ro_points = Collections.unmodifiableSet(this.points);
	private Owner owner;
	private final ReplicateListenerContainer rep = new ReplicateListenerContainer();
	
	EditableEdge(){name = "E_" + AbstractNamed.generateName();}
	
	EditableEdge(Owner owner){
		this();
		this.setOwner(owner);
	}
	
	@Override
	public String toString() {
		return this.name;
	}
		
	@Override
	public Set<? extends Point> getPoints() {
		return this.ro_points;
	}

	@Override
	public Set<EditablePoint> getEditablePoints() {
		return this.points;
	}

	@Override
	public void linkPoint(EditablePoint point) {
		if(point == null)
			return;
		this.points.add(point);
		if(this != point.getHandle()){
			point.setHandle(this);
		}
	}

	@Override
	public void unlinkPoint(EditablePoint point) {
		if(point == null)
			return;
		this.points.remove(point);
		if(this == point.getHandle())
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

	@Override
	public void setOwner(Owner value){
		this.owner = value;
	}

	@Override
	public int hashCode() {
		final int prime = 83;
		return prime * name.hashCode();
	}
	
}