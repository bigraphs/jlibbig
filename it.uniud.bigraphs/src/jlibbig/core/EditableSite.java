package jlibbig.core;

class EditableSite implements EditableChild, Site{
	
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
			if(!this.parent.equals(parent)){
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
	
}