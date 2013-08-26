package jlibbig.core;

import java.util.*;


class EditableOuterName extends EditableLinkFacet implements OuterName, EditableNamed, EditableHandle{

	private Set<EditablePoint> points = new HashSet<>();
	private final Set<? extends Point> ro_points = Collections.unmodifiableSet(this.points);
	private Owner owner;
	
	EditableOuterName(String name){
		super(name);
	}
	
	EditableOuterName(){super();}
	
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
	public void setOwner(Owner value){
		this.owner = value;
	}
}