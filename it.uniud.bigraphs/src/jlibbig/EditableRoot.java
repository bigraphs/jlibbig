package jlibbig;

import java.util.*;

class EditableRoot implements EditableParent, Root{
	
	private Set<EditableChild> children = new HashSet<>();
	private final Set<Child> ro_chd;
	
	@SuppressWarnings("unchecked")
	EditableRoot(){
		ro_chd = (Set<Child>) (Set<? extends Child>)  Collections.unmodifiableSet(this.children);
	}

	@Override
	public Set<Child> getChildren() {
		return this.ro_chd;
	}
	
	@Override
	public void addChild(EditableChild child) {
		if(child == null)
			return;
		this.children.add(child);
		if(!this.equals(child.getParent())){
			child.setParent(this);
		}
	}

	@Override
	public void removeChild(EditableChild child) {
		if(child == null)
			return;
		this.children.remove(child);
		if(this.equals(child.getParent()))
				child.setParent(null);
	}
	
	public Set<EditableChild> getEditableChildren(){
		return this.children;
	}	
	
	@Override
	public EditableRoot replicate(){
		return new EditableRoot();
	}
}
