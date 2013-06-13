package jlibbig.core;

class EditableSite implements EditableChild, Site, PlaceEntity{
	
	private EditableParent parent;
	
	EditableSite(){}
	
	EditableSite(EditableParent parent){
		this.setParent(parent);
	}
	
	@Override
	public EditableParent getParent() {
		return parent;
	}
	
	@Override
	public Owner getOwner() {
		return (parent == null) ? null : parent.getOwner();
	}

	@Override
	public void setParent(EditableParent parent){
		if(this.parent != null){
			if(this.parent != parent){
				EditableParent p = this.parent;
				this.parent = parent;
				p.removeChild(this);
			}
		}
		this.parent = parent;
		if(parent != null){
			parent.addChild(this);
		}
	}
	
	@Override
	public EditableSite replicate(){
		return new EditableSite();
	}
	
	@Override
	public boolean isParent() {
		return false;
	}

	@Override
	public boolean isChild() {
		return true;
	}

	@Override
	public boolean isRoot() {
		return false;
	}

	@Override
	public boolean isSite() {
		return true;
	}

	@Override
	public boolean isNode() {
		return false;
	}
	
}