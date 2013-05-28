package jlibbig.core;

import java.util.*;

class EditableOuterName extends EditableLinkFacet implements OuterName, EditableNamed, EditableHandle{

	private Set<EditablePoint> points;
	private final Set<Point> ro_points;
	private Owner owner;
	
	@SuppressWarnings("unchecked")
	EditableOuterName(String name){
		super(name);
		this.points = new HashSet<>();
		this.ro_points = (Set<Point>) (Set<? extends Point>)  Collections.unmodifiableSet(this.points);
	}
	
	@SuppressWarnings("unchecked")
	EditableOuterName(){
		this.points = new HashSet<>();
		this.ro_points = (Set<Point>) (Set<? extends Point>)  Collections.unmodifiableSet(this.points);
	}
	
	@Override
	public Set<Point> getPoints() {
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
		if(!this.equals(point.getHandle())){
			point.setHandle(this);
		}
	}

	@Override
	public void unlinkPoint(EditablePoint point) {
		if(point == null)
			return;
		this.points.remove(point);
		if(this.equals(point.getHandle()))
			point.setHandle(null);
	}
	

	@Override
	public EditableOuterName replicate() {
		return new EditableOuterName(this.getName());
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