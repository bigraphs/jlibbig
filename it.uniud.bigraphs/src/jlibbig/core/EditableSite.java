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
	public void setParent(EditableParent parent){
		if(this.parent != null){
			if(!this.parent.equals(parent)){
				this.parent.removeChild(this);
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