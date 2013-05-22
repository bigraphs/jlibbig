package jlibbig;

import java.util.*;

class EditableEdge implements Edge, EditableHandle, Replicable{

	private Set<EditablePoint> points;
	private final Set<Point> ro_points;
	
	@SuppressWarnings("unchecked")
	EditableEdge(){
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
	public EditableEdge replicate() {
		return new EditableEdge();
	}
	
}